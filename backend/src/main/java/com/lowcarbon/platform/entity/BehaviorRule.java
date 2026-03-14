package com.lowcarbon.platform.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "behavior_rules")
public class BehaviorRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 80)
    private String name;

    @Column(length = 300)
    private String description;

    @Column(nullable = false)
    private Integer pointsPerAction;

    @Column(nullable = false)
    private Double carbonReductionPerAction;

    @Column(nullable = false)
    private Integer dailyLimit;

    @Column(nullable = false)
    private Boolean active = true;
}
