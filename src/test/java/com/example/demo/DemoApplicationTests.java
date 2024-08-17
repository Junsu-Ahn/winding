package com.example.demo;

import com.example.demo.member.entity.Member;
import com.example.demo.member.repository.MemberRepository;
import com.example.demo.post.entity.Post;
import com.example.demo.post.repository.PostRepository;
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

	@Test
	void contextLoads() {
	}

	@Test
	void createTestPosts() {
		// 데이터베이스에서 첫 번째 회원 가져오기 (테스트 용도로)
		Member member = memberRepository.findById(1L).orElseThrow(() -> new IllegalArgumentException("Invalid member Id"));

		// 테스트용 게시물 10개 생성
		for (int i = 1; i <= 10; i++) {
			Post post = new Post();
			post.setMember(member);
			post.setTitle("테스트 제목 " + i);
			post.setDescription("이것은 테스트 내용입니다. " + i);
			post.setDeparture("대전");
			post.setDepartureLat(36.351);
			post.setDepartureLng(127.385);
			post.setDestination("전주");
			post.setDestinationLat(35.825);
			post.setDestinationLng(127.15);
			post.setAuthor(member.getUsername());
			post.setViews(0);

			// 경유지 추가 (테스트용)
			post.addWaypoint("경유지 " + i, 36.5 + i * 0.01, 127.0 + i * 0.01);

			postRepository.save(post);
		}

		// 저장된 게시물 확인
		List<Post> posts = postRepository.findAll();
		System.out.println("총 게시물 수: " + posts.size());
		for (Post p : posts) {
			System.out.println(p.getTitle() + " - " + p.getDescription());
		}
	}
}
