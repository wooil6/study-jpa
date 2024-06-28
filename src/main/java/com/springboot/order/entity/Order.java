package com.springboot.order.entity;

import com.springboot.member.entity.Member;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
@Getter
@Setter
@Entity(name = "ORDERS")
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long orderId;

    @Enumerated(EnumType.STRING)
    private OrderStatus orderStatus = OrderStatus.ORDER_REQUEST;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(nullable = false, name = "LAST_MODIFIED_AT")
    private LocalDateTime modifiedAt = LocalDateTime.now();

    @ManyToOne // 다대일 관계 설정
    @JoinColumn(name = "MEMBER_ID") // 외래키 설정
    private Member member; // 회원 정보

    @OneToMany(mappedBy = "order") // 일대다 관계 설정
    // 주문 커피 리스트 초기화
    private List<OrderCoffee> orderCoffees = new ArrayList<>();

    //OrderCoffee를 Order에 추가하는 메서드
    public void setOrderCoffee(OrderCoffee orderCoffee) {
        orderCoffees.add(orderCoffee); // 리스트에 추가
        if (orderCoffee.getOrder() != this) {
            orderCoffee.setOrder(this); // OrderCoffee에 Order설정
        }
    }

    //Order에 Member를 설정하는 메서드
    public void setMember(Member member) {
        this.member = member; // 멤버 설정
        // 현재 Order 객체의 member필드
        // member는 전달받은 Member객체

        // Member 객체의 orders 리스트를 반환하고 그 리스트에
        // Order객체가 포함되어 있는지 확인
        if (!member.getOrders().contains(this)) {
            // Order 객체를 orders 리스트에 추가
            member.setOrder(this); // Memeber에 Order 설정
        }
    }


    public void addMember(Member member) {
        this.member = member;
    }

    public enum OrderStatus {
        ORDER_REQUEST(1, "주문 요청"),
        ORDER_CONFIRM(2, "주문 확정"),
        ORDER_COMPLETE(3, "주문 처리 완료"),
        ORDER_CANCEL(4, "주문 취소");

        @Getter
        private int stepNumber;

        @Getter
        private String stepDescription;

        OrderStatus(int stepNumber, String stepDescription) {
            this.stepNumber = stepNumber;
            this.stepDescription = stepDescription;
        }
    }
}