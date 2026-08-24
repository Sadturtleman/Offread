package com.android.offread.library.domain.model

/**
 * F-007: 아이템을 다른 컬렉션으로 이동할 때 원 컬렉션 용어맵 처리 방식.
 * 용어맵은 컬렉션 스코프이므로 원 컬렉션의 용어 전체가 대상이 된다.
 */
enum class TermMapMoveStrategy {
    /** 함께 이동: 원 컬렉션의 용어를 대상 컬렉션으로 옮긴다. 원문이 겹치면 대상 컬렉션 용어를 우선한다. */
    MOVE,

    /** 병합: 원 컬렉션 용어를 대상 컬렉션에 복사한다. 원문이 겹치면 대상 우선, 원 컬렉션 용어는 유지. */
    MERGE,

    /** 남겨두기: 용어맵은 건드리지 않는다. */
    LEAVE,
}
