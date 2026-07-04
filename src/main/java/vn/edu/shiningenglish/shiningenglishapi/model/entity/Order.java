package vn.edu.shiningenglish.shiningenglishapi.model.entity;

import jakarta.persistence.*;
import vn.edu.shiningenglish.shiningenglishapi.enums.OrderStatus;
import vn.edu.shiningenglish.shiningenglishapi.enums.PaymentMethod;

import java.time.LocalDateTime;

@Entity
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "total_amount")
    private Integer totalAmount = 0;

    @Enumerated(EnumType.STRING)
    @Column(length = 50)
    private OrderStatus status = OrderStatus.pending;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", length = 50)
    private PaymentMethod paymentMethod = PaymentMethod.cod;

    @Column(name = "payment_reference")
    private String paymentReference;

    @Column(name = "payment_checkout_url", length = 500)
    private String paymentCheckoutUrl;

    @Column(name = "payment_metadata", columnDefinition = "JSON")
    private String paymentMetadata;

    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    @Column(name = "placed_at")
    private LocalDateTime placedAt;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public Integer getTotalAmount() { return totalAmount; }
    public void setTotalAmount(Integer totalAmount) { this.totalAmount = totalAmount; }
    public OrderStatus getStatus() { return status; }
    public void setStatus(OrderStatus status) { this.status = status; }
    public PaymentMethod getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(PaymentMethod paymentMethod) { this.paymentMethod = paymentMethod; }
    public String getPaymentReference() { return paymentReference; }
    public void setPaymentReference(String paymentReference) { this.paymentReference = paymentReference; }
    public String getPaymentCheckoutUrl() { return paymentCheckoutUrl; }
    public void setPaymentCheckoutUrl(String paymentCheckoutUrl) { this.paymentCheckoutUrl = paymentCheckoutUrl; }
    public String getPaymentMetadata() { return paymentMetadata; }
    public void setPaymentMetadata(String paymentMetadata) { this.paymentMetadata = paymentMetadata; }
    public LocalDateTime getPaidAt() { return paidAt; }
    public void setPaidAt(LocalDateTime paidAt) { this.paidAt = paidAt; }
    public LocalDateTime getPlacedAt() { return placedAt; }
    public void setPlacedAt(LocalDateTime placedAt) { this.placedAt = placedAt; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
