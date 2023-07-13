package com.Devex.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.stereotype.Repository;

import com.Devex.Entity.ProductVariant;

@EnableJpaRepositories
@Repository("productVariantRepository")
public interface ProductVariantRepository extends JpaRepository<ProductVariant, Integer> {

}
