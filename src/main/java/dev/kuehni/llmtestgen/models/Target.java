package dev.kuehni.llmtestgen.models;

import jakarta.annotation.Nonnull;
import jakarta.persistence.*;

import java.util.Objects;

@Entity
@Table
public class Target extends BaseModel {

    @ManyToOne
    @JoinColumn(name = "run_id", nullable = false)
    private Run run;

    @Column(nullable = false)
    private String className;

    protected Target() {}

    public Target(@Nonnull Run run, @Nonnull String className) {
        this.run = Objects.requireNonNull(run, "run");
        this.className = Objects.requireNonNull(className, "className");
    }

    public Run getRun() {
        return run;
    }

    public String getClassName() {
        return className;
    }

}
