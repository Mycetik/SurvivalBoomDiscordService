package net.survivalboom.sbds.modules.test.database;

import jakarta.persistence.*;
import net.survivalboom.sbds.api.database.DataRecord;

@Entity
@Table(name = "testmodule_test")
public class TestRecord extends DataRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(nullable = false)
    private long hash;

    @Column(nullable = false)
    private long magicNumber;


    public TestRecord(long hash, long magicNumber) {
        this.hash = hash;
        this.magicNumber = magicNumber;
    }

    protected TestRecord() {}


    public long getId() {
        return id;
    }

    public long getHash() {
        return hash;
    }

    public long getMagicNumber() {
        return magicNumber;
    }

}
