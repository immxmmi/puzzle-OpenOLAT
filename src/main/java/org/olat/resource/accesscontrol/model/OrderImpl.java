/**
 * <a href="http://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, http://www.frentix.com
 * <p>
 */
package org.olat.resource.accesscontrol.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.persistence.Transient;
import jakarta.persistence.Version;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.olat.basesecurity.IdentityImpl;
import org.olat.core.id.Identity;
import org.olat.core.id.ModifiedInfo;
import org.olat.core.id.Persistable;
import org.olat.core.util.StringHelper;
import org.olat.resource.accesscontrol.BillingAddress;
import org.olat.resource.accesscontrol.Order;
import org.olat.resource.accesscontrol.OrderPart;
import org.olat.resource.accesscontrol.OrderStatus;
import org.olat.resource.accesscontrol.Price;

/**
 * 
 * Description:<br>
 * The order contains a list of order part. Every Order part links
 * a set of order lines to a payment.
 * 
 * <P>
 * Initial Date:  19 avr. 2011 <br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
@Entity(name="acorder")
@Table(name="o_ac_order")
public class OrderImpl implements Persistable, Order, ModifiedInfo {

	private static final long serialVersionUID = 2982829081818496553L;
	
	@Id
	@GeneratedValue(generator = "system-uuid")
	@GenericGenerator(name = "system-uuid", strategy = "enhanced-sequence", parameters={
		@Parameter(name="sequence_name", value="hibernate_unique_key"),
		@Parameter(name="force_table_use", value="true"),
		@Parameter(name="optimizer", value="legacy-hilo"),
		@Parameter(name="value_column", value="next_hi"),
		@Parameter(name="increment_size", value="32767"),
		@Parameter(name="initial_value", value="32767")
	})
	@Column(name="order_id", nullable=false, unique=true, insertable=true, updatable=false)
	private Long key;
	@Version
	private int version = 0;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="creationdate", nullable=false, insertable=true, updatable=false)
	private Date creationDate;
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="lastmodified", nullable=false, insertable=true, updatable=true)
	private Date lastModified;
	
	@Column(name="is_valid", nullable=true, insertable=true, updatable=true)
	private boolean valid;
	@Column(name="order_status", nullable=true, insertable=true, updatable=true)
	private String orderStatus;
	@Column(name="purchase_order_number", nullable=true, insertable=true, updatable=true)
	private String purchaseOrderNumber;
	@Column(name="order_comment", nullable=true, insertable=true, updatable=true)
	private String comment;
	
	@Embedded
    @AttributeOverride(name="amount", column = @Column(name="total_amount"))
    @AttributeOverride(name="currencyCode", column = @Column(name="total_currency_code"))
	private PriceImpl total;
	@Embedded
    @AttributeOverride(name="amount", column = @Column(name="total_lines_amount"))
    @AttributeOverride(name="currencyCode", column = @Column(name="total_lines_currency_code"))
	private PriceImpl totalOrderLines;
	@Embedded
    @AttributeOverride(name="amount", column = @Column(name="discount_amount"))
    @AttributeOverride(name="currencyCode", column = @Column(name="discount_currency_code"))
	private PriceImpl discount;
	@Embedded
    @AttributeOverride(name="amount", column = @Column(name="cancellation_fee_amount"))
    @AttributeOverride(name="currencyCode", column = @Column(name="cancellation_fee_currency_code"))
	private PriceImpl cancellationFees;
	@Embedded
    @AttributeOverride(name="amount", column = @Column(name="cancellation_fee_lines_amount"))
    @AttributeOverride(name="currencyCode", column = @Column(name="cancellation_fee_lines_currency_code"))
	private PriceImpl cancellationFeesLines;
	
	@Transient
	private String currencyCode;

	@ManyToOne(targetEntity=IdentityImpl.class,fetch=FetchType.LAZY,optional=true)
	@JoinColumn(name="fk_delivery_id", nullable=false, insertable=true, updatable=false)
	private Identity delivery;
	
	@OneToMany(targetEntity=OrderPartImpl.class, fetch=FetchType.LAZY,
			orphanRemoval=true, cascade={CascadeType.PERSIST, CascadeType.REMOVE})
	@JoinColumn(name="fk_order_id")
	@OrderColumn(name="pos")
	private List<OrderPart> parts;
	
	@ManyToOne(targetEntity=BillingAddressImpl.class,fetch=FetchType.LAZY,optional=true)
	@JoinColumn(name="fk_billing_address", nullable=true, insertable=true, updatable=true)
	private BillingAddress billingAddress;
	
	public OrderImpl() {
		//
	}
	
	@Override
	public Long getKey() {
		return key;
	}

	public void setKey(Long key) {
		this.key = key;
	}

	@Override
	public Date getCreationDate() {
		return creationDate;
	}

	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}

	@Override
	public String getOrderNr() {
		return getKey() == null ? "" : getKey().toString();
	}

	@Override
	public boolean isValid() {
		return valid;
	}

	public void setValid(boolean valid) {
		this.valid = valid;
	}
	
	@Override
	public Identity getDelivery() {
		return delivery;
	}

	public void setDelivery(Identity delivery) {
		this.delivery = delivery;
	}

	@Override
	public OrderStatus getOrderStatus() {
		if(StringHelper.containsNonWhitespace(orderStatus)) {
			return OrderStatus.valueOf(orderStatus);
		}
		return null;
	}
	
	public void setOrderStatus(OrderStatus status) {
		if(status == null) {
			orderStatus = null;
		} else {
			orderStatus = status.name();
		}
	}

	public String getOrderStatusStr() {
		return orderStatus;
	}

	public void setOrderStatusStr(String orderStatus) {
		this.orderStatus = orderStatus;
	}

	@Override
	public String getPurchaseOrderNumber() {
		return purchaseOrderNumber;
	}

	@Override
	public void setPurchaseOrderNumber(String purchaseOrderNumber) {
		this.purchaseOrderNumber = purchaseOrderNumber;
	}

	@Override
	public String getComment() {
		return comment;
	}

	@Override
	public void setComment(String comment) {
		this.comment = comment;
	}

	@Override
	public Date getLastModified() {
		return lastModified;
	}

	@Override
	public void setLastModified(Date lastModified) {
		this.lastModified = lastModified;
	}
	
	@Override
	public String getCurrencyCode() {
		return currencyCode;
	}
	
	public void setCurrencyCode(String currencyCode) {
		this.currencyCode = currencyCode;
	}

	@Override
	public Price getTotal() {
		return total;
	}

	@Override
	public void setTotal(Price total) {
		this.total = (PriceImpl)total;
	}

	@Override
	public Price getTotalOrderLines() {
		return totalOrderLines;
	}

	public void setTotalOrderLines(Price totalOrderLines) {
		this.totalOrderLines = (PriceImpl)totalOrderLines;
	}

	@Override
	public Price getDiscount() {
		return discount;
	}

	public void setDiscount(Price discount) {
		this.discount = (PriceImpl)discount;
	}
	
	@Override
	public Price getCancellationFees() {
		return cancellationFees;
	}

	@Override
	public void setCancellationFees(Price cancellationFees) {
		this.cancellationFees = (PriceImpl)cancellationFees;
	}

	@Override
	public PriceImpl getCancellationFeesLines() {
		return cancellationFeesLines;
	}

	@Override
	public void setCancellationFeesLines(Price cancellationFeesLines) {
		this.cancellationFeesLines = (PriceImpl)cancellationFeesLines;
	}

	@Override
	public List<OrderPart> getParts() {
		if(parts == null) {
			parts = new ArrayList<>();
		}
		return parts;
	}

	public void setParts(List<OrderPart> parts) {
		this.parts = parts;
	}
	
	@Override
	public void recalculate() {
		// Should be only calculated when the order is created.
		totalOrderLines = new PriceImpl(BigDecimal.ZERO, getCurrencyCode());
		for(OrderPart part : getParts()) {
			((OrderPartImpl)part).recalculate(getCurrencyCode());
			totalOrderLines = totalOrderLines.add(part.getTotalOrderLines());
		}
		
		// The total can be adjusted by an administrative person.
		if (total == null) {
			total = totalOrderLines.clone();
		}
		if(discount == null) {
			discount = new PriceImpl(BigDecimal.ZERO, getCurrencyCode());
		}
	}

	@Override
	public BillingAddress getBillingAddress() {
		return billingAddress;
	}

	public void setBillingAddress(BillingAddress billingAddress) {
		this.billingAddress = billingAddress;
	}

	@Override
	public int hashCode() {
		return getKey() == null ? 27591 : getKey().hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if(this == obj) {
			return true;
		}
		if(obj instanceof OrderImpl order) {
			return getKey() != null && getKey().equals(order.getKey());
		}
		return false;
	}

	@Override
	public boolean equalsByPersistableKey(Persistable persistable) {
		return equals(persistable);
	}
}
