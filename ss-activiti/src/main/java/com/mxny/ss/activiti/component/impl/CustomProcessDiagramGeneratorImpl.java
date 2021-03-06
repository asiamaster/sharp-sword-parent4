package com.mxny.ss.activiti.component.impl;

import com.mxny.ss.activiti.component.CustomProcessDiagramCanvas;
import com.mxny.ss.activiti.component.CustomProcessDiagramGenerator;
import com.mxny.ss.activiti.consts.ActivitiConstants;
import org.activiti.bpmn.model.Process;
import org.activiti.bpmn.model.*;
import org.activiti.image.impl.DefaultProcessDiagramGenerator;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import javax.imageio.stream.ImageOutputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;

@Component
public class CustomProcessDiagramGeneratorImpl extends DefaultProcessDiagramGenerator implements CustomProcessDiagramGenerator {
    //预初始化流程图绘制，大大提升了系统启动后首次查看流程图的速度
    static {
        new CustomProcessDiagramCanvas(10,10,0,0,"png", ActivitiConstants.FONT_NAME,ActivitiConstants.FONT_NAME,ActivitiConstants.FONT_NAME,null);
    }

    @Override
    public InputStream generateDiagram(BpmnModel bpmnModel, String imageType, List<String> highLightedActivities,
                                       List<String> highLightedFlows, String activityFontName, String labelFontName, String annotationFontName,
                                       ClassLoader customClassLoader, double scaleFactor, Color[] colors) {
        CustomProcessDiagramCanvas customProcessDiagramCanvas = generateProcessDiagram(bpmnModel, imageType, highLightedActivities, highLightedFlows,
                activityFontName, labelFontName, annotationFontName, customClassLoader, scaleFactor,colors);
        BufferedImage bufferedImage = customProcessDiagramCanvas.generateBufferedImage(imageType);
        ByteArrayOutputStream bs = new ByteArrayOutputStream();
        ImageOutputStream imOut;
        try {
            imOut = ImageIO.createImageOutputStream(bs);
            ImageIO.write(bufferedImage, "PNG", imOut);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new ByteArrayInputStream(bs.toByteArray());
    }

    @Override
    public BufferedImage generatePngImage(BpmnModel bpmnModel) {
        return generatePngImage(bpmnModel, 1.0D);
    }

    /**
     * 画普通流程图(非追踪图)
     * @param bpmnModel
     * @param imageType
     * @param activityFontName
     * @param labelFontName
     * @param annotationFontName
     * @param customClassLoader
     * @return
     */
    @Override
    public InputStream generateDiagram(BpmnModel bpmnModel, String imageType, String activityFontName, String labelFontName, String annotationFontName, ClassLoader customClassLoader) {
        return generateDiagram(bpmnModel, imageType, Collections.<String>emptyList(), Collections.<String>emptyList(),
                activityFontName, labelFontName, annotationFontName, customClassLoader, 1.0, new Color[] {Color.BLACK, Color.BLACK});
    }

    protected void drawActivity(CustomProcessDiagramCanvas processDiagramCanvas, BpmnModel bpmnModel, FlowNode flowNode,
                                List<String> highLightedActivities, List<String> highLightedFlows, double scaleFactor, Color[] colors) {
        ActivityDrawInstruction drawInstruction = activityDrawInstructions.get(flowNode.getClass());
        if (drawInstruction != null) {
            //边界事件和捕获事件不处理图形，由下面的代码单独处理
            if (!(flowNode instanceof IntermediateCatchEvent) && !(flowNode instanceof BoundaryEvent)) {
                drawInstruction.draw(processDiagramCanvas, bpmnModel, flowNode);
                if(flowNode instanceof StartEvent || flowNode instanceof EndEvent) {
                    GraphicInfo graphicInfo = bpmnModel.getLocationMap().get(flowNode.getId());
                    //这里由于会有Y坐标的位移，必须深拷贝一个对象出来
                    GraphicInfo graphicInfoCopy = new GraphicInfo();
                    BeanUtils.copyProperties(graphicInfo, graphicInfoCopy);
                    //在开始和结束节点下方32像素位置绘制label
                    graphicInfoCopy.setY(graphicInfoCopy.getY() + 32D);
                    // Draw label
                    processDiagramCanvas.drawEventLabel(flowNode.getName(), graphicInfoCopy, true);
                }
            }
            // Gather info on the multi instance marker
            boolean multiInstanceSequential = false, multiInstanceParallel = false, collapsed = false;
            if (flowNode instanceof Activity) {
                Activity activity = (Activity) flowNode;
                MultiInstanceLoopCharacteristics multiInstanceLoopCharacteristics = activity.getLoopCharacteristics();
                if (multiInstanceLoopCharacteristics != null) {
                    multiInstanceSequential = multiInstanceLoopCharacteristics.isSequential();
                    multiInstanceParallel = !multiInstanceSequential;
                }
            }
            // Gather info on the collapsed marker
            GraphicInfo graphicInfo = bpmnModel.getGraphicInfo(flowNode.getId());
            if (flowNode instanceof SubProcess) {
                collapsed = graphicInfo.getExpanded() != null && !graphicInfo.getExpanded();
            } else if (flowNode instanceof CallActivity) {
                collapsed = true;
            }
            if (scaleFactor == 1.0) {
                // Actually draw the markers
                processDiagramCanvas.drawActivityMarkers((int) graphicInfo.getX(), (int) graphicInfo.getY(),(int) graphicInfo.getWidth(), (int) graphicInfo.getHeight(),
                        multiInstanceSequential, multiInstanceParallel, collapsed);
            }
            // Draw highlighted activities
            if (highLightedActivities.contains(flowNode.getId())) {
                if(flowNode.getId().equals(highLightedActivities.get(highLightedActivities.size()-1))
                        && !(flowNode instanceof EndEvent)) {//非结束节点，并且是当前节点
                    drawHighLight((flowNode instanceof StartEvent), processDiagramCanvas, bpmnModel.getGraphicInfo(flowNode.getId()), colors[1]);
                }else {//普通节点
                    drawHighLight((flowNode instanceof StartEvent)||(flowNode instanceof EndEvent),processDiagramCanvas, bpmnModel.getGraphicInfo(flowNode.getId()), colors[0]);
                }
            }
        }
        // Outgoing transitions of activity
        for (SequenceFlow sequenceFlow : flowNode.getOutgoingFlows()) {
            String flowId = sequenceFlow.getId();
            boolean highLighted = (highLightedFlows.contains(flowId));
            String defaultFlow = null;
            if (flowNode instanceof Activity) {
                defaultFlow = ((Activity) flowNode).getDefaultFlow();
            } else if (flowNode instanceof Gateway) {
                defaultFlow = ((Gateway) flowNode).getDefaultFlow();
            }

            boolean isDefault = false;
            if (defaultFlow != null && defaultFlow.equalsIgnoreCase(flowId)) {
                isDefault = true;
            }
//	      boolean drawConditionalIndicator = sequenceFlow.getConditionExpression() != null && !(flowNode instanceof Gateway);
            String sourceRef = sequenceFlow.getSourceRef();
            String targetRef = sequenceFlow.getTargetRef();
            FlowElement sourceElement = bpmnModel.getFlowElement(sourceRef);
            FlowElement targetElement = bpmnModel.getFlowElement(targetRef);
            List<GraphicInfo> graphicInfoList = bpmnModel.getFlowLocationGraphicInfo(flowId);
            if (graphicInfoList != null && graphicInfoList.size() > 0) {
                graphicInfoList = connectionPerfectionizer(processDiagramCanvas, bpmnModel, sourceElement, targetElement, graphicInfoList);
                int xPoints[]= new int[graphicInfoList.size()];
                int yPoints[]= new int[graphicInfoList.size()];

                for (int i=1; i<graphicInfoList.size(); i++) {
                    GraphicInfo graphicInfo = graphicInfoList.get(i);
                    GraphicInfo previousGraphicInfo = graphicInfoList.get(i-1);
                    if (i == 1) {
                        xPoints[0] = (int) previousGraphicInfo.getX();
                        yPoints[0] = (int) previousGraphicInfo.getY();
                    }
                    xPoints[i] = (int) graphicInfo.getX();
                    yPoints[i] = (int) graphicInfo.getY();

                }
                //画高亮线
                processDiagramCanvas.drawSequenceflow(xPoints, yPoints, false, isDefault, highLighted, scaleFactor, colors[0]);

                // Draw sequenceflow label
//	        GraphicInfo labelGraphicInfo = bpmnModel.getLabelGraphicInfo(flowId);
//	        if (labelGraphicInfo != null) {
//	          processDiagramCanvas.drawLabel(sequenceFlow.getName(), labelGraphicInfo, false);
//	        }else {//解决流程图连线名称不显示的BUG
                GraphicInfo lineCenter = getLineCenter(graphicInfoList);
                processDiagramCanvas.drawLabel(highLighted, sequenceFlow.getName(), lineCenter, Math.abs(xPoints[1]-xPoints[0]) >= 5);
//	        }
            }
        }
        // Nested elements
        if (flowNode instanceof FlowElementsContainer) {
            for (FlowElement nestedFlowElement : ((FlowElementsContainer) flowNode).getFlowElements()) {
                if (nestedFlowElement instanceof FlowNode) {
                    drawActivity(processDiagramCanvas, bpmnModel, (FlowNode) nestedFlowElement,
                            highLightedActivities, highLightedFlows, scaleFactor);
                }
            }
        }
        //下面的代码单独处理边界事件和捕获事件，解决文字压到图标上的缺陷
        if (flowNode instanceof IntermediateCatchEvent) {
            //这里由于会有Y坐标的位移，必须深拷贝一个对象出来
            GraphicInfo graphicInfo = new GraphicInfo();
            BeanUtils.copyProperties(bpmnModel.getGraphicInfo(flowNode.getId()), graphicInfo);
            IntermediateCatchEvent intermediateCatchEvent = (IntermediateCatchEvent) flowNode;
            if (intermediateCatchEvent.getEventDefinitions() != null && !intermediateCatchEvent.getEventDefinitions().isEmpty()) {
                if (intermediateCatchEvent.getEventDefinitions().get(0) instanceof SignalEventDefinition) {
                    processDiagramCanvas.drawCatchingSignalEvent(graphicInfo, true, scaleFactor);
                    graphicInfo.setY(graphicInfo.getY()-12D);
                    processDiagramCanvas.drawEventLabel(flowNode.getName(), graphicInfo);
                } else if (intermediateCatchEvent.getEventDefinitions().get(0) instanceof TimerEventDefinition) {
                    processDiagramCanvas.drawCatchingTimerEvent(graphicInfo, true, scaleFactor);
                    graphicInfo.setY(graphicInfo.getY()-12D);
                    processDiagramCanvas.drawEventLabel(flowNode.getName(), graphicInfo);
                } else if (intermediateCatchEvent.getEventDefinitions().get(0) instanceof MessageEventDefinition) {
                    processDiagramCanvas.drawCatchingMessageEvent(graphicInfo, true, scaleFactor);
                    graphicInfo.setY(graphicInfo.getY()-12D);
                    processDiagramCanvas.drawEventLabel(flowNode.getName(), graphicInfo);
                }
            }
        }
        if(flowNode instanceof BoundaryEvent){
            //这里由于会有Y坐标的位移，必须深拷贝一个对象出来
            GraphicInfo graphicInfo = new GraphicInfo();
            BeanUtils.copyProperties(bpmnModel.getGraphicInfo(flowNode.getId()), graphicInfo);
            BoundaryEvent boundaryEvent = (BoundaryEvent)flowNode;
            if (boundaryEvent.getEventDefinitions() != null && !boundaryEvent.getEventDefinitions().isEmpty()) {
                if (boundaryEvent.getEventDefinitions().get(0) instanceof TimerEventDefinition) {
                    processDiagramCanvas.drawCatchingTimerEvent(graphicInfo, boundaryEvent.isCancelActivity(), scaleFactor);
                    graphicInfo.setY(graphicInfo.getY()-12D);
                    processDiagramCanvas.drawEventLabel(flowNode.getName(), graphicInfo);
                } else if (boundaryEvent.getEventDefinitions().get(0) instanceof ErrorEventDefinition) {
                    processDiagramCanvas.drawCatchingErrorEvent(graphicInfo, boundaryEvent.isCancelActivity(), scaleFactor);
                } else if (boundaryEvent.getEventDefinitions().get(0) instanceof SignalEventDefinition) {
                    processDiagramCanvas.drawCatchingSignalEvent(graphicInfo, boundaryEvent.isCancelActivity(), scaleFactor);
                    graphicInfo.setY(graphicInfo.getY()-12D);
                    processDiagramCanvas.drawEventLabel(flowNode.getName(), graphicInfo);
                } else if (boundaryEvent.getEventDefinitions().get(0) instanceof MessageEventDefinition) {
                    processDiagramCanvas.drawCatchingMessageEvent(graphicInfo, boundaryEvent.isCancelActivity(), scaleFactor);
                    graphicInfo.setY(graphicInfo.getY()-12D);
                    processDiagramCanvas.drawEventLabel(flowNode.getName(), graphicInfo);
                } else if (boundaryEvent.getEventDefinitions().get(0) instanceof CompensateEventDefinition) {
                    processDiagramCanvas.drawCatchingCompensateEvent(graphicInfo, boundaryEvent.isCancelActivity(), scaleFactor);
                }
            }
        }
    }
    protected void drawHighLight(boolean isStartOrEnd, CustomProcessDiagramCanvas processDiagramCanvas, GraphicInfo graphicInfo, Color color) {
        processDiagramCanvas.drawHighLight(isStartOrEnd, (int) graphicInfo.getX(), (int) graphicInfo.getY(), (int) graphicInfo.getWidth(), (int) graphicInfo.getHeight(), color);
    }

    protected static CustomProcessDiagramCanvas initProcessDiagramCanvas(BpmnModel bpmnModel, String imageType,
                                                                         String activityFontName, String labelFontName, String annotationFontName, ClassLoader customClassLoader) {
        // We need to calculate maximum values to know how big the image will be in its entirety
        double minX = Double.MAX_VALUE;
        double maxX = 0;
        double minY = Double.MAX_VALUE;
        double maxY = 0;

        for (Pool pool : bpmnModel.getPools()) {
            GraphicInfo graphicInfo = bpmnModel.getGraphicInfo(pool.getId());
            minX = graphicInfo.getX();
            maxX = graphicInfo.getX() + graphicInfo.getWidth();
            minY = graphicInfo.getY();
            maxY = graphicInfo.getY() + graphicInfo.getHeight();
        }

        List<FlowNode> flowNodes = gatherAllFlowNodes(bpmnModel);
        for (FlowNode flowNode : flowNodes) {
            GraphicInfo flowNodeGraphicInfo = bpmnModel.getGraphicInfo(flowNode.getId());
            // width
            if (flowNodeGraphicInfo.getX() + flowNodeGraphicInfo.getWidth() > maxX) {
                maxX = flowNodeGraphicInfo.getX() + flowNodeGraphicInfo.getWidth();
            }
            if (flowNodeGraphicInfo.getX() < minX) {
                minX = flowNodeGraphicInfo.getX();
            }
            // height
            if (flowNodeGraphicInfo.getY() + flowNodeGraphicInfo.getHeight() > maxY) {
                maxY = flowNodeGraphicInfo.getY() + flowNodeGraphicInfo.getHeight();
            }
            if (flowNodeGraphicInfo.getY() < minY) {
                minY = flowNodeGraphicInfo.getY();
            }

            for (SequenceFlow sequenceFlow : flowNode.getOutgoingFlows()) {
                List<GraphicInfo> graphicInfoList = bpmnModel.getFlowLocationGraphicInfo(sequenceFlow.getId());
                if (graphicInfoList != null) {
                    for (GraphicInfo graphicInfo : graphicInfoList) {
                        // width
                        if (graphicInfo.getX() > maxX) {
                            maxX = graphicInfo.getX();
                        }
                        if (graphicInfo.getX() < minX) {
                            minX = graphicInfo.getX();
                        }
                        // height
                        if (graphicInfo.getY() > maxY) {
                            maxY = graphicInfo.getY();
                        }
                        if (graphicInfo.getY()< minY) {
                            minY = graphicInfo.getY();
                        }
                    }
                }
            }
        }
        List<Artifact> artifacts = gatherAllArtifacts(bpmnModel);
        for (Artifact artifact : artifacts) {
            GraphicInfo artifactGraphicInfo = bpmnModel.getGraphicInfo(artifact.getId());
            if (artifactGraphicInfo != null) {
                // width
                if (artifactGraphicInfo.getX() + artifactGraphicInfo.getWidth() > maxX) {
                    maxX = artifactGraphicInfo.getX() + artifactGraphicInfo.getWidth();
                }
                if (artifactGraphicInfo.getX() < minX) {
                    minX = artifactGraphicInfo.getX();
                }
                // height
                if (artifactGraphicInfo.getY() + artifactGraphicInfo.getHeight() > maxY) {
                    maxY = artifactGraphicInfo.getY() + artifactGraphicInfo.getHeight();
                }
                if (artifactGraphicInfo.getY() < minY) {
                    minY = artifactGraphicInfo.getY();
                }
            }
            List<GraphicInfo> graphicInfoList = bpmnModel.getFlowLocationGraphicInfo(artifact.getId());
            if (graphicInfoList != null) {
                for (GraphicInfo graphicInfo : graphicInfoList) {
                    // width
                    if (graphicInfo.getX() > maxX) {
                        maxX = graphicInfo.getX();
                    }
                    if (graphicInfo.getX() < minX) {
                        minX = graphicInfo.getX();
                    }
                    // height
                    if (graphicInfo.getY() > maxY) {
                        maxY = graphicInfo.getY();
                    }
                    if (graphicInfo.getY()< minY) {
                        minY = graphicInfo.getY();
                    }
                }
            }
        }
        int nrOfLanes = 0;
        for (Process process : bpmnModel.getProcesses()) {
            for (Lane l : process.getLanes()) {
                nrOfLanes++;
                GraphicInfo graphicInfo = bpmnModel.getGraphicInfo(l.getId());
                // // width
                if (graphicInfo.getX() + graphicInfo.getWidth() > maxX) {
                    maxX = graphicInfo.getX() + graphicInfo.getWidth();
                }
                if (graphicInfo.getX() < minX) {
                    minX = graphicInfo.getX();
                }
                // height
                if (graphicInfo.getY() + graphicInfo.getHeight() > maxY) {
                    maxY = graphicInfo.getY() + graphicInfo.getHeight();
                }
                if (graphicInfo.getY() < minY) {
                    minY = graphicInfo.getY();
                }
            }
        }
        // Special case, see https://activiti.atlassian.net/browse/ACT-1431
        if (flowNodes.isEmpty() && bpmnModel.getPools().isEmpty() && nrOfLanes == 0) {
            // Nothing to show
            minX = 0;
            minY = 0;
        }
        // 左右预留25像素，避免label显示不全
        // 上下预留少量空间，避免有些图标显示不全
        return new CustomProcessDiagramCanvas((int) maxX + 25,(int) maxY + 10, (int) minX -25, (int) minY-10,
                imageType, activityFontName, labelFontName, annotationFontName, customClassLoader);
    }

    private CustomProcessDiagramCanvas generateProcessDiagram(BpmnModel bpmnModel, String imageType,
                                                              List<String> highLightedActivities, List<String> highLightedFlows, String activityFontName,
                                                              String labelFontName, String annotationFontName, ClassLoader customClassLoader, double scaleFactor,
                                                              Color [] colors) {
        if(null == highLightedActivities) {
            highLightedActivities = Collections.<String>emptyList();
        }
        if(null == highLightedFlows) {
            highLightedFlows = Collections.<String>emptyList();
        }
        prepareBpmnModel(bpmnModel);
        CustomProcessDiagramCanvas processDiagramCanvas = initProcessDiagramCanvas(bpmnModel, imageType, activityFontName, labelFontName, annotationFontName, customClassLoader);
        // Draw pool shape, if process is participant in collaboration
        for (Pool pool : bpmnModel.getPools()) {
            GraphicInfo graphicInfo = bpmnModel.getGraphicInfo(pool.getId());
            processDiagramCanvas.drawPoolOrLane(pool.getName(), graphicInfo);
        }
        // Draw lanes
        for (Process process : bpmnModel.getProcesses()) {
            for (Lane lane : process.getLanes()) {
                GraphicInfo graphicInfo = bpmnModel.getGraphicInfo(lane.getId());
                processDiagramCanvas.drawPoolOrLane(lane.getName(), graphicInfo);
            }
        }
        // Draw activities and their sequence-flows
        for (Process process: bpmnModel.getProcesses()) {
            List<FlowNode> flowNodeList= process.findFlowElementsOfType(FlowNode.class);
            for (FlowNode flowNode : flowNodeList) {
                drawActivity(processDiagramCanvas, bpmnModel, flowNode, highLightedActivities, highLightedFlows, scaleFactor, colors);
            }
        }
        // Draw artifacts
        for (Process process : bpmnModel.getProcesses()) {
            for (Artifact artifact : process.getArtifacts()) {
                drawArtifact(processDiagramCanvas, bpmnModel, artifact);
            }
            List<SubProcess> subProcesses = process.findFlowElementsOfType(SubProcess.class, true);
            if (subProcesses != null) {
                for (SubProcess subProcess : subProcesses) {
                    for (Artifact subProcessArtifact : subProcess.getArtifacts()) {
                        drawArtifact(processDiagramCanvas, bpmnModel, subProcessArtifact);
                    }
                }
            }
        }
        return processDiagramCanvas;
    }

}
