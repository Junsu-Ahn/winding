package com.example.demo;

import com.example.demo.market.product.ProductService;
import com.example.demo.member.entity.Member;
import com.example.demo.member.repository.MemberRepository;
import com.example.demo.post.entity.Post;
import com.example.demo.post.repository.PostRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
class DemoApplicationTests {

	@Autowired
	private PostRepository postRepository;

	@Autowired
	private MemberRepository memberRepository;

	@Autowired
	private ProductService productService;

	@Test
	@DisplayName("마켓 제품 생성")
	void test1() {
		for (int i = 1; i <= 200; i++) {
			String name = String.format("테스트 상품:[%03d]", i);
			int price = 1;
			productService.create(name, price);
		}
	}
}