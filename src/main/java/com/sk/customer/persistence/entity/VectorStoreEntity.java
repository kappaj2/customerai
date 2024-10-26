package com.sk.customer.persistence.entity;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Array;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.Map;
import java.util.UUID;

@Data
@Entity(name = "VectorStoreEntity")
@Table(name = "vector_store")
@NoArgsConstructor
@AllArgsConstructor
public class VectorStoreEntity {

     @Id
     @GeneratedValue(strategy = GenerationType.AUTO)
     @Column(name = "id", columnDefinition = "uuid default uuid_generate_v4()")
     private UUID id;

     @Column(name = "content", nullable = false)
     private String content;

     @Column(name = "metadata", columnDefinition = "json")
     @JdbcTypeCode(SqlTypes.JSON)
     private Map<String, Object> metadata;

     @Column
     @JdbcTypeCode(SqlTypes.VECTOR)
     @Array(length = 3) // dimensions
     private float[] embedding;
}
