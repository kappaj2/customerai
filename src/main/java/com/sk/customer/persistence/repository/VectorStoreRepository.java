package com.sk.customer.persistence.repository;

import com.sk.customer.persistence.entity.VectorStoreEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VectorStoreRepository extends JpaRepository<VectorStoreEntity, String> {
}
