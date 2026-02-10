package dev.kuehni.llmtestgen.models;

import io.ebean.Model;
import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;

@MappedSuperclass
public abstract class BaseModel extends Model {

    @Id
    @GeneratedValue
    @Column(nullable = false)
    protected long id;

}
