// 직무 상황 카테고리 (주문 받기, 상품 안내 등) 정보를 DB에서 조회하기 위한 레포지토리
package com.example.demo.training.repository;

import com.example.demo.entity.TrainingCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TrainingCategoryRepository extends JpaRepository<TrainingCategory, Long> {
    // 기본적인 findAll, findById 등은 JpaRepository에서 제공하므로
    // 추가적인 쿼리 메소드가 당장은 필요 x
}
