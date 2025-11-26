package com.example.demo.repository;

import com.example.demo.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.List;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    // 최상위 카테고리 조회(parent_id == null인 경우)
    List<Category> findByParentIsNull();

    // 특정 카테고리의 하위 카테고리 조회
    List<Category> findByParent(Category parent);

    // 카테고리 이름으로 검색
    Category findByName(String name);

    @Query("SELECT c FROM Category c LEFT JOIN FETCH c.children WHERE c.parent IS NULL")
    List<Category> findAllTopCategory();
}