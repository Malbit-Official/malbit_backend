// 직무 상황 카테고리 (주문 받기, 상품 안내 등) 정보를 DB에서 조회하기 위한 레포지토리
// 카테고리 목록을 가져올 때 사용
package com.example.demo.training.repository;

import com.example.demo.entity.TrainingCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TrainingCategoryRepository extends JpaRepository<TrainingCategory, Long> {

}
