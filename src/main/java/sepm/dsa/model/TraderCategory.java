package sepm.dsa.model;

import org.hibernate.validator.constraints.NotBlank;

import javax.persistence.*;
import javax.validation.constraints.Size;
import java.util.HashMap;
import java.util.Map;

@Entity
@Table(name = "traderCategories")
public class TraderCategory implements BaseModel {
    private static final long serialVersionUID = 2957793850231481713L;

    @Id
    @GeneratedValue
    @Column(nullable = false, unique = true)
    private Integer id;

    @NotBlank
    @Size(max = 60, min = 1)
    @Column(nullable = false, length = 60)
    private String name;

    @Size(max = 1000)
    @Column(length = 1000)
    private String comment;

//    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
//    @JoinColumn(name = "tradercategory_id", nullable = false)
//    private Set<AssortmentNature> assortments = new HashSet<>();


    @OneToMany(fetch = FetchType.LAZY, mappedBy = "pk.traderCategory", cascade = { CascadeType.REMOVE, CascadeType.ALL }, orphanRemoval = true)
    @MapKey(name="pk.productCategory")
    private Map<ProductCategory, AssortmentNature> assortments = new HashMap<>();

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Map<ProductCategory, AssortmentNature> getAssortments() {
        return assortments;
    }

    public void setAssortments(Map<ProductCategory, AssortmentNature> assortments) {
        this.assortments = assortments;
    }

    public void putAssortment(AssortmentNature assortment) {
        this.assortments.put(assortment.getProductCategory(), assortment);
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TraderCategory that = (TraderCategory) o;

        if (id != null ? !id.equals(that.id) : that.id != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

	@Override
	public String toString() { return name; }
}
