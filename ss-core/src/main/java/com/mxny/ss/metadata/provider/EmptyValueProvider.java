package com.mxny.ss.metadata.provider;

import com.mxny.ss.metadata.FieldMeta;
import com.mxny.ss.metadata.ValuePair;
import com.mxny.ss.metadata.ValueProvider;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * 空值提供者
 */
@Component
public class EmptyValueProvider implements ValueProvider {
	@Override
	public String getDisplayText(Object val, Map metadata, FieldMeta fieldMeta) {
		return "";
	}

	@Override
    public List<ValuePair<?>> getLookupList(Object val, Map metadata, FieldMeta fieldMeta) {
		return null;
	}
}
