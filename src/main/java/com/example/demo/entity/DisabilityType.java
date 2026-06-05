package com.example.demo.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum DisabilityType {
    LANGUAGE("언어장애"),
    CRANIAL_NERVE("뇌신경장애"),
    HEARING("청각장애"),
    ARTICULATION("조음장애"),
    CONDUCTIVE_HEARING("전음성 난청"),
    SENSORINEURAL_HEARING("감음신경성 난청"),
    FUNCTIONAL_VOICE("기능성 발성장애"),
    LARYNGEAL("후두장애"),
    ORAL("구강장애");

    private final String description;
}
