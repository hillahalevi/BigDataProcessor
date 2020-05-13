package com.zoomInfo.model.mapper;


import com.zoomInfo.model.Entry;
import org.springframework.batch.item.file.mapping.FieldSetMapper;
import org.springframework.batch.item.file.transform.FieldSet;
import org.springframework.stereotype.Component;

@Component
public class EntryFieldSetMapper implements FieldSetMapper<Entry> {
    @Override
    public Entry mapFieldSet(FieldSet fieldSet) {
        final Entry entry = new Entry();
        entry.setId(fieldSet.readRawString("id"));
        entry.setFirstName(fieldSet.readRawString("firstName"));
        entry.setLastName(fieldSet.readRawString("lastName"));
        entry.setAge(fieldSet.readInt("age"));
        return entry;
    }
}
