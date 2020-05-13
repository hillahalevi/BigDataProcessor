package com.zoomInfo.chuncks.config;

import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemStream;
import org.springframework.batch.item.ItemStreamException;
import org.springframework.batch.item.ItemWriter;

import java.util.List;

public class SynchronizedWrapperWriter<String> implements ItemWriter<String>, ItemStream {

    private ItemWriter<String> itemWriter;
    private boolean isStream = false;

    public SynchronizedWrapperWriter(ItemWriter<String> itemWriter) {
        this.itemWriter = itemWriter;
        if (itemWriter instanceof ItemStream) {
            isStream = true;
        }
    }

    @Override
    public void open(ExecutionContext executionContext) throws ItemStreamException {
        if (isStream) {
            ((ItemStream) itemWriter).open(new ExecutionContext());
        }
    }

    @Override
    public void update(ExecutionContext executionContext) throws ItemStreamException {

    }

    @Override
    public void close() throws ItemStreamException {
        if (isStream) {
            ((ItemStream) itemWriter).close();
        }
    }

    @Override
    public synchronized void write(List<? extends String> list) throws Exception {
        itemWriter.write(list);
    }
}
