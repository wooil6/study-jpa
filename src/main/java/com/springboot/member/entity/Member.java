package com.springboot.member.entity;

import com.springboot.order.entity.Order;
import com.springboot.stamp.Stamp;
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
@Entity // JPA 엔티티로 선언
public class Member {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // 기본기 자동으로 생성
    private Long memberId;

    @Column(nullable = false, updatable = false, unique = true)
    private String email;

    @Column(length = 100, nullable = false)
    private String name;

    @Column(length = 13, nullable = false, unique = true)
    private String phone;

    // 추가된 부분
    @Enumerated(value = EnumType.STRING) // Enum타입을 데이터베이스에 저장하는데 값을 문자열로 저장
    @Column(length = 20, nullable = false)
    private MemberStatus memberStatus = MemberStatus.MEMBER_ACTIVE;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(nullable = false, name = "LAST_MODIFIED_AT")
    private LocalDateTime modifiedAt = LocalDateTime.now();

    @OneToMany(mappedBy = "member") // 회원과 주문 관계로 멤버가 소유
    // 회원이 주문 내역을 조회할 때 리스트로 가지고 있음
    private List<Order> orders = new ArrayList<>();

    @OneToOne(cascade = CascadeType.ALL) // 회원과 스탬프의 관계로 스탬프가 소유
    // cascade = CascadeType.ALL - 부모 엔티티의 변경, 삭제 등이 자식 엔티티에도 영향
    // 스탬프는 회원을 꼭 거쳐서만 수정 등이 가능하기에 영속성 접근을 해야함
    @JoinColumn(name = "STAMP_ID")
    private Stamp stamp = new Stamp(); // 새로 가입하면 새로운 크폰을 받게 되어 새롭게 stamp를 만들기

    public Member(String email) {
        this.email = email;
    }

    public Member(String email, String name, String phone) {
        this.email = email;
        this.name = name;
        this.phone = phone;
    }

    // 주문을 추가하고, 주문의 회원이 현재 회원이 아닌 경우 현재 회원으로 설정
    public void setOrder(Order order) {
        //해당 회원의 주문 목록에 새로운 주문을 추가하는 역할
        orders.add(order); // orders라는 List<Order>에 새로운 Order객체 order를 추가
        // order객체의 회원을 현재 Member객체(this)로 설정
        if (order.getMember() != this) {
            order.setMember(this);
        }
    }

    // 추가 된 부분
    public enum MemberStatus {
        MEMBER_ACTIVE("활동중"),
        MEMBER_SLEEP("휴면 상태"),
        MEMBER_QUIT("탈퇴 상태");

        @Getter
        private String status;

        MemberStatus(String status) {
           this.status = status;
        }
    }
}
