package com.kevindeyne.datascrambler.helper;

import com.kevindeyne.datascrambler.dao.TargetConnectionDao;
import com.kevindeyne.datascrambler.domain.distributionmodel.TableData;
import lombok.Data;
import org.jooq.DSLContext;

@Data
public class ApplyContext {

    private DSLContext dsl;
    private TargetConnectionDao targetConnectionDao;
    private TableData table;
    private boolean tableExists;
    private int factor;
    private boolean clean;

    public ApplyContext(int factor, boolean clean) {
        this.factor = factor; this.clean = clean;
    }

    public ApplyContext withTable(TableData table, boolean tableExists) {
        table.setTotalCount(table.getTotalCount()*this.factor);
        this.table = table;
        this.tableExists = tableExists;
        return this;
    }
}