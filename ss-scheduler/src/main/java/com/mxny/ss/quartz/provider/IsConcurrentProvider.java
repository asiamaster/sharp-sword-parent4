package com.mxny.ss.quartz.provider;

import com.mxny.ss.metadata.FieldMeta;
import com.mxny.ss.metadata.ValuePair;
import com.mxny.ss.metadata.ValuePairImpl;
import com.mxny.ss.metadata.ValueProvider;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 由MyBatis Generator工具自动生成
 * This file was generated on 2017-10-24 09:32:32.
 */
@Component
public class IsConcurrentProvider implements ValueProvider {
    private static final List<ValuePair<?>> buffer;

    static {
        buffer = new ArrayList<ValuePair<?>>();
        buffer.add(new ValuePairImpl("同步", 0));
        buffer.add(new ValuePairImpl("异步", 1));
    }

    @Override
    public List<ValuePair<?>> getLookupList(Object obj, Map metaMap, FieldMeta fieldMeta) {
        return buffer;
    }

    @Override
    public String getDisplayText(Object obj, Map metaMap, FieldMeta fieldMeta) {
        if(obj == null || "".equals(obj)) return null;
        for(ValuePair<?> valuePair : buffer){
            if(obj.equals(valuePair.getValue())){
                return valuePair.getText();
            }
        }
        return null;
    }
}