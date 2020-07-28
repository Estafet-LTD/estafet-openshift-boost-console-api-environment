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

import com.estafet.openshift.boost.messages.environments.Environments;

@Entity
@Table(name = "Product")
public class Product {
    
    @Id
	@Column(name = "PRODUCT_ID", nullable = false)
    private String productId;

	@Column(name = "DESCRIPTION", nullable = false)
    private String description;

	@Column(name = "VERSION", nullable = false)
    private String version;
    
    @Column(name = "UPDATED_DATE", nullable = false)
	private String updatedDate;

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

    public void setUpdatedDate(String updatedDate) {
		this.updatedDate = updatedDate;
	}

	public List<Env> getEnvs() {
        return envs;
    }

	public Product addEnv(Env env) {
		env.setProduct(this);
		envs.add(env);
		return this;
    }
	
	public Product addEnvs(List<Env> envs) {
		for (Env env : envs) {
			addEnv(env);
		}
		return this;
	}
	
	public Environments getEnvironments() {
		Environments environments = Environments.builder()
				.setProductId(productId)
				.setUpdatedDate(updatedDate)
				.build();
		for (Env env : envs) {
			environments.addEnvironment(env.getEnvironment());
		}
		return environments;
	}

    public static ProductBuilder builder() {
        return new ProductBuilder();
    }
    
    public static class ProductBuilder {

        private String productId;
        private String description;
        private String version;
        private String updatedDate;

        public ProductBuilder setUpdatedDate(String updatedDate) {
			this.updatedDate = updatedDate;
			return this;
		}

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
            product.setUpdatedDate(updatedDate);
            return product;
        }
        
    }

}