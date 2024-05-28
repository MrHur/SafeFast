package org.zerock.safefast.entity;


import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.DynamicUpdate;

import java.time.LocalDate;
import java.util.List;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString(exclude = {"coOpCompany", "item", "progressCheckItems"})
@DynamicUpdate
public class PurchaseOrder {
    @Id
    @Column
    private String purchOrderNumber;

    @Column
    private LocalDate purchOrderDate = LocalDate.now();

    @Column
    private Integer purchOrderQuantity;

    @Column
    private String note;

    @Column
    private LocalDate receiveDuedate;

    @Column
    private Integer purchProgress;

    @Column
    private String procPlanNumber;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "businessNumber", referencedColumnName = "businessNumber")
    @JsonManagedReference
    private CoOpCompany coOpCompany;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "itemCode")
    @JsonManagedReference
    private Item item;

    @OneToMany(mappedBy = "purchaseOrder", fetch = FetchType.LAZY)
    @JsonIgnore
    private List<ProgressCheckItem> progressCheckItems;

    public String getBusinessNumber() {
        return coOpCompany != null ? coOpCompany.getBusinessNumber() : null;
    }

    public String getItemCode() {
        return item != null ? item.getItemCode() : null;
    }

    public void setBusinessNumber(String businessNumber) {
        if (this.coOpCompany == null) {
            this.coOpCompany = new CoOpCompany();
        }
        this.coOpCompany.setBusinessNumber(businessNumber);
    }

    public void setItemCode(String itemCode) {
        this.item = item;
    }
}
