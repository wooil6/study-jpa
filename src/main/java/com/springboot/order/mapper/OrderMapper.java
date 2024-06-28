package com.springboot.order.mapper;

import com.springboot.coffee.entity.Coffee;
import com.springboot.member.entity.Member;
import com.springboot.order.dto.OrderCoffeeResponseDto;
import com.springboot.order.dto.OrderPatchDto;
import com.springboot.order.dto.OrderPostDto;
import com.springboot.order.dto.OrderResponseDto;
import com.springboot.order.entity.Order;
import com.springboot.order.entity.OrderCoffee;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;
import java.util.stream.Collectors;


@Mapper(componentModel = "spring") // 인터페이스가 스프링에서 사용할 수 있는 매퍼
public interface OrderMapper {

    //OrderPostDto 객체를 Order로 변환
   default Order orderPostDtoToOrder(OrderPostDto orderPostDto) {

       // 새로운 주문을 생성하고, 데이터베이스에 저장하기 위해 새로운 객체 생성
       Order order = new Order();
       Member member = new Member();

       //OrderPostDto에서 회원 ID를 가져와서 Member객체에 설정
       member.setMemberId(orderPostDto.getMemberId());
       //생성된 Member객체를 Order객체에 설정
       order.setMember(member);

       // OrderPostDto에서 OrderCoffeeDto 리스트를 가져와서 OrderCoffee리스트로 변환
       List<OrderCoffee> orderCoffees = orderPostDto.getOrderCoffees().stream()
               .map(orderCoffeeDto -> { // 각 OrderCoffeeDto객체를 OrderCoffee 객체로 변환
                   OrderCoffee orderCoffee = new OrderCoffee();
                   // OrderCoffeeDto에서 수량을 가져와서 OrderCoffee 객체에 설정
                   orderCoffee.setQuantity(orderCoffeeDto.getQuantity());
                   Coffee coffee = new Coffee();
                   coffee.setCoffeeId(orderCoffeeDto.getCoffeeId());
                   orderCoffee.setCoffee(coffee); // 생성된 coffee 객체를 orderCoffee 객체에 설정
                   orderCoffee.setOrder(order) ; // OrderCoffee 객체에 해당 order 객체를 설정
                   return orderCoffee; // 변환된 orderCoffee 반환
               }).collect(Collectors.toList()); //변환된 OrderCoffee 객체를 리스트로 수집
       order.setOrderCoffees(orderCoffees); // 생성된 OrderCoffee 리스트를 Order 객체에 설정
       return order; // 변환된 order객체 반환
   }


    Order orderPatchDtoToOrder(OrderPatchDto orderPatchDto);

   // 매핑을 지정하는 어노테이션
    // source - 원본 객체의 필드
    // target - 대상 객체의 필드
    // Order의 member.memberId를 OrderResponseDto의 memberId로 매핑
    //
    @Mapping(source = "member.memberId", target = "memberId")
   OrderResponseDto orderToOrderResponseDto(Order order);
    List<OrderResponseDto> ordersToOrderResponseDtos(List<Order> orders);

    @Mapping(source = "coffee.coffeeId", target = "coffeeId")// 출발지점.
    @Mapping(source = "coffee.korName", target = "korName")
    @Mapping(source = "coffee.engName", target = "engName")
    @Mapping(source = "coffee.price", target = "price")
    OrderCoffeeResponseDto orderCoffeeToOrderCoffeeResponseDto(OrderCoffee orderCoffee);
}
