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
    private long fill;
    private boolean clean;

    public ApplyContext(int factor, long fill, boolean clean) {
        this.factor = factor;
        this.fill = fill;
        this.clean = clean;
    }

    public ApplyContext withTable(TableData table, boolean tableExists) {
        if(this.fill == 0) {
            table.setTotalCount(table.getTotalCount()*this.factor);
        } else {
            table.setTotalCount(this.fill);
        }
        this.table = table;
        this.tableExists = tableExists;
        return this;
    }
}
