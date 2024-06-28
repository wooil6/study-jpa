package com.springboot.order.service;

import com.springboot.coffee.service.CoffeeService;
import com.springboot.exception.BusinessLogicException;
import com.springboot.exception.ExceptionCode;
import com.springboot.member.entity.Member;
import com.springboot.member.service.MemberService;
import com.springboot.order.entity.Order;
import com.springboot.order.entity.OrderCoffee;
import com.springboot.order.repository.OrderRepository;
import com.springboot.stamp.Stamp;
import org.aspectj.weaver.ast.Or;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class OrderService {
    private final MemberService memberService;
    private final OrderRepository orderRepository;
    private final CoffeeService coffeeService;

    public OrderService(MemberService memberService,
                        OrderRepository orderRepository,
                        CoffeeService coffeeService) {
        this.memberService = memberService;
        this.orderRepository = orderRepository;
        this.coffeeService = coffeeService;
    }

    public Order createOrder(Order order) {
        // 주문 내용 검증 - 멤버와 커피가 존재하는지
        verifyOrder(order);

        //주문한 회원의 스탬프 정보 업데이트
        updateStamp(order);

        // 주분 정보를 데이터베이스에 저장하고 저장된 Order객체 반환
        return orderRepository.save(order);
    }

    // 메서드 추가
    public Order updateOrder(Order order) {
        Order findOrder = findVerifiedOrder(order.getOrderId());

        Optional.ofNullable(order.getOrderStatus())
                .ifPresent(orderStatus -> findOrder.setOrderStatus(orderStatus));
        findOrder.setModifiedAt(LocalDateTime.now());
        return orderRepository.save(findOrder);
    }

    public Order findOrder(long orderId) {
        return findVerifiedOrder(orderId);
    }

    public Page<Order> findOrders(int page, int size) {
        return orderRepository.findAll(PageRequest.of(page, size,
                Sort.by("orderId").descending()));
    }

    public void cancelOrder(long orderId) {
        Order findOrder = findVerifiedOrder(orderId);
        int step = findOrder.getOrderStatus().getStepNumber();

        // OrderStatus의 step이 2 이상일 경우(ORDER_CONFIRM)에는 주문 취소가 되지 않도록한다.
        if (step >= 2) {
            throw new BusinessLogicException(ExceptionCode.CANNOT_CHANGE_ORDER);
        }
        findOrder.setOrderStatus(Order.OrderStatus.ORDER_CANCEL);
        findOrder.setModifiedAt(LocalDateTime.now());
        orderRepository.save(findOrder);
    }

    private Order findVerifiedOrder(long orderId) {
        Optional<Order> optionalOrder = orderRepository.findById(orderId);
        Order findOrder =
                optionalOrder.orElseThrow(() ->
                        new BusinessLogicException(ExceptionCode.ORDER_NOT_FOUND));
        return findOrder;
    }


    private void verifyOrder(Order order) { // Order객체를 받아서 주문에 포함된 멤버와 커피가 유효한지 확인
        // 주문 객체에서 회원 Id를 가져와서 유효한 회원인지 확인
        memberService.findVerifiedMember(order.getMember().getMemberId());

        // 주문 객체에서 주문 커피 리스트를 가져와서 각 커피에 대해 유효한 커피인지 확인
        order.getOrderCoffees().stream()
                // coffeeService.findVerifiedCoffee를 호출하여 커피 항목이 유효한지 확인
                .forEach(orderCoffee -> coffeeService.findVerifiedCoffee(
                        // 각 OrderCoffee 객체에서 커피 객체의 ID를 가져와 유효한 커피인지 확인
                        orderCoffee.getCoffee().getCoffeeId()
                ));
    }

    //Order 객체를 받아서 해당 회원의 스탬프를 업데이트
    private void updateStamp(Order order) {
        // 주문 객체에서 회원 ID를 가져와 해당 회원 객체를 찾음
        Member member = memberService.findMember(order.getMember().getMemberId());

        // 주문한 커피의 수량을 합산하여 스탬프 개수 계산
        int orderStampCount = order.getOrderCoffees().stream()
                // 각 OrderCoffee 객체에서 수량을 get
                .map(orderCoffee -> orderCoffee.getQuantity())
                // 수량을 int로 변환
                .mapToInt(quantity -> quantity)
                // 모든 수량을 더해서 총 수량 계산
                .sum();

        //회원의 스탬프 객체를 가져오기
        Stamp stamp = member.getStamp();
        // 스탬프 개수를 주문한 커피 수만큼 증가
        stamp.setStampCount(stamp.getStampCount() + orderStampCount);
        // 스탬프 수정 시간 변경
        stamp.setModifiedAt(LocalDateTime.now());
        // 수정된 회원 정보 업데이트
        memberService.updateMember(member);
    }
}
