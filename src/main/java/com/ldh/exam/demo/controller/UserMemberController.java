package com.ldh.exam.demo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.ldh.exam.demo.service.MemberService;
import com.ldh.exam.demo.util.Ut;
import com.ldh.exam.demo.vo.Member;
import com.ldh.exam.demo.vo.ResultData;
import com.ldh.exam.demo.vo.Rq;

@Controller
public class UserMemberController {

	private MemberService memberService;
	private Rq rq;

	public UserMemberController(MemberService memberService, Rq rq) {
		this.memberService = memberService;
		this.rq = rq;
	}

	// 회원등록 페이지 보기 메서드
	@RequestMapping("/user/member/join")
	public String showJoin() {
		return "user/member/join";
	}

	// 회원 등록하기 메서드
	@RequestMapping("/user/member/doJoin")
	@ResponseBody
	public String doJoin(String loginId, String loginPw, String nickname, String cellphoneNo, String email) {

		// 입력 데이터 유효성 검사
		if (Ut.empty(loginId)) {
			return rq.jsHistoryBack("아이디(을)를 입력해주세요.");
		}

		if (Ut.empty(loginPw)) {
			return rq.jsHistoryBack("비밀번호(을)를 입력해주세요.");
		}

		if (Ut.empty(nickname)) {
			return rq.jsHistoryBack("닉네임(을)를 입력해주세요.");
		}

		if (Ut.empty(cellphoneNo)) {
			return rq.jsHistoryBack("연락처(을)를 입력해주세요.");
		}

		if (Ut.empty(email)) {
			return rq.jsHistoryBack("이메일(을)를 입력해주세요.");
		}

		// 회원 등록하기
		ResultData<Integer> joinMemberRd = memberService.doJoin(loginId, loginPw, nickname, cellphoneNo, email);

		// 아이디 또는 닉네임, 이메일 중복 확인
		if (joinMemberRd.isFail()) {
			return rq.jsHistoryBack(joinMemberRd.getMsg());
		}

		return rq.jsReplace(Ut.f("%s 님의 회원등록이 완료되었습니다.", nickname), "/");
	}

	// 회원 로그인 페이지 메서드
	@RequestMapping("/user/member/login")
	public String showLogin() {

		return "user/member/login";
	}

	// 회원 로그인 메서드
	@RequestMapping("/user/member/doLogin")
	@ResponseBody
	public String doLogin(String loginId, String loginPw) {

		// 로그인 확인
		if (rq.isLogined() == true) {
			return rq.jsHistoryBack("이미 로그인 중입니다.");
		}

		// 입력 데이터 유효성 검사
		if (Ut.empty(loginId)) {
			return rq.jsHistoryBack("아이디(을)를 입력해주세요.");
		}

		if (Ut.empty(loginPw)) {
			return rq.jsHistoryBack("비밀번호(을)를 입력해주세요.");
		}

		// 회원정보 가져오기
		Member member = memberService.getMemberByLoginId(loginId);

		if (member == null) {
			return rq.jsReplace("등록되지 않은 회원입니다.", "/user/member/login");
		}

		if (member.getLoginPw().equals(loginPw) == false) {
			return rq.jsReplace("잘못된 비밀번호입니다.", "/user/member/login");
		}

		// 로그인 하기
		rq.login(member);

		return rq.jsReplace(Ut.f("%s님 환영합니다.", member.getNickname()), "/");
	}

	// 회원 로그아웃 메서드
	@RequestMapping("/user/member/doLogout")
	@ResponseBody
	public String doLogout() {

		// 로그아웃 확인
		if (rq.isLogined() == false) {
			return rq.jsHistoryBack("이미 로그아웃 중입니다.");
		}

		// 로그아웃 하기
		rq.logout();

		return rq.jsReplace("로그아웃되었습니다.", "/");
	}

	// My홈 페이지 메서드
	@RequestMapping("/user/member/myPage")
	public String showMyPage() {

		return "user/member/myPage";
	}

	// 비밀번호 확인 페이지 메서드
	@RequestMapping("/user/member/checkPassword")
	public String showCheckPW() {

		return "user/member/checkPassword";
	}

	// 비밀번호 확인 메서드
	@RequestMapping("/user/member/doCheckPassword")
	@ResponseBody
	public String doCheckPassword(String loginPw, String replaceUri) {

		// 입력데이터 유효성 검사
		if (Ut.empty(loginPw)) {
			return rq.jsHistoryBack("비밀번호(을)를 입력해주세요.");
		}

		// 비밀번호 확인하기
		if (rq.getLoginedMember().getLoginPw().equals(loginPw) == false) {
			return rq.jsHistoryBack("잘못된 비밀번호 입니다.");
		}

		return rq.jsReplace("", replaceUri);
	}

	// 회원정보 수정 페이지 메서드
	@RequestMapping("/user/member/modify")
	public String showModify() {

		return "user/member/modify";
	}
}
