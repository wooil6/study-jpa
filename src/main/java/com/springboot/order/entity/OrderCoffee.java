package com.springboot.order.entity;

import com.springboot.coffee.entity.Coffee;
import com.springboot.member.entity.Member;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Getter
@Setter
@Entity
@NoArgsConstructor
public class OrderCoffee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long orderCoffeeId;

    @Column(nullable = false)
    private int quantity;

    @ManyToOne
    @JoinColumn(name = "COFFEE_ID")
    private Coffee coffee;

    @ManyToOne
    @JoinColumn(name = "ORDERS_ID")
    private Order order;

    // Order에 OrderCoffee를 설정하는 메서드
    public void setOrder(Order order) {
        this.order = order;

        if (!order.getOrderCoffees().contains(this)) {
            order.setOrderCoffee(this);
        }
    }

    public OrderCoffee(int quantity) {
        this.quantity = quantity;
    }
}
