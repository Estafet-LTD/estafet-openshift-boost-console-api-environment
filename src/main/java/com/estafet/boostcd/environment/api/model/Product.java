package com.estafet.boostcd.environment.api.model;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

@Entity
@Table(name = "Product")
public class Product {
    
    @Id
	@Column(name = "PRODUCT_ID", nullable = false)
    private String productId;

    @Id
	@Column(name = "DESCRIPTION", nullable = false)
    private String description;

    @Id
	@Column(name = "VERSION", nullable = false)
    private String version;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<Env> envs = new ArrayList<Env>();

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public List<Env> getEnvs() {
        return envs;
    }

	public Product addEnv(Env env) {
		env.setProduct(this);
		envs.add(env);
		return this;
    }

    public static ProductBuilder builder() {
        return new ProductBuilder();
    }
    
    public static class ProductBuilder {

        private String productId;
        private String description;
        private String version;

        public ProductBuilder setProductId(String productId) {
            this.productId = productId;
            return this;
        }

        public ProductBuilder setDescription(String description) {
            this.description = description;
            return this;
        }

        public ProductBuilder setVersion(String version) {
            this.version = version;
            return this;
        }

        public Product build() {
            Product product = new Product();
            product.setDescription(description);
            product.setProductId(productId);
            product.setVersion(version);
            return product;
        }
        
    }

}