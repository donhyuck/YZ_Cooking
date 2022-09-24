package com.ldh.exam.demo.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.ldh.exam.demo.service.MemberService;
import com.ldh.exam.demo.service.RecipeService;
import com.ldh.exam.demo.util.Ut;
import com.ldh.exam.demo.vo.Member;
import com.ldh.exam.demo.vo.Recipe;
import com.ldh.exam.demo.vo.ResultData;
import com.ldh.exam.demo.vo.Rq;

@Controller
public class UserMemberController {

	private MemberService memberService;
	private RecipeService recipeService;
	private Rq rq;

	public UserMemberController(MemberService memberService, RecipeService recipeService, Rq rq) {
		this.memberService = memberService;
		this.recipeService = recipeService;
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
	public String doJoin(String loginId, String loginPw, String nickname, String cellphoneNo, String email,
			@RequestParam(defaultValue = "/") String afterLoginUri) {

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

		String afterJoinUri = "/user/member/login?afterLoginUri=" + Ut.getUriEncoded(afterLoginUri);

		return rq.jsReplace(Ut.f("%s 님의 회원등록이 완료되었습니다. 로그인 페이지로 이동합니다.", nickname), afterJoinUri);
	}

	// 회원 로그인 페이지 메서드
	@RequestMapping("/user/member/login")
	public String showLogin() {

		return "user/member/login";
	}

	// 회원 로그인 메서드
	@RequestMapping("/user/member/doLogin")
	@ResponseBody
	public String doLogin(String loginId, String loginPw, @RequestParam(defaultValue = "/") String afterLoginUri) {

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
		
		if (member.isDelStatus() == true) {
			return rq.jsReplace("탈퇴회원입니다. 재등록이 필요합니다.", "/user/member/join");
		}

		// 로그인 하기
		rq.login(member);

		String msg = Ut.f("%s님 환영합니다.", member.getNickname());

		// 임시 비밀번호 사용시 비밀번호 변경 권유
		boolean isUsingTempPassword = memberService.isUsingTempPassword(member.getId());

		if (isUsingTempPassword) {
			msg = "임시 비밀번호를 변경해주세요.";
			afterLoginUri = "/user/member/myPage";
		}

		return rq.jsReplace(msg, afterLoginUri);
	}

	// 회원 로그아웃 메서드
	@RequestMapping("/user/member/doLogout")
	@ResponseBody
	public String doLogout(@RequestParam(defaultValue = "/") String afterLogoutUri) {

		// 로그아웃 확인
		if (rq.isLogined() == false) {
			return rq.jsHistoryBack("이미 로그아웃 중입니다.");
		}

		// 로그아웃 하기
		rq.logout();

		return rq.jsReplace("로그아웃되었습니다.", afterLogoutUri);
	}

	// My홈 페이지 메서드
	@RequestMapping("/user/member/myPage")
	public String showMyPage(Model model) {

		List<Recipe> haveReplyRecipes = recipeService.getHaveReplyRecipes(rq.getLoginedMemberId());

		model.addAttribute("haveReplyRecipes", haveReplyRecipes);

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

		// 비밀번호 확인에 대한 인증코드 발급
		if (replaceUri.equals("../member/modify")) {
			String authKeyForModify = memberService.genAuthKey(rq.getLoginedMemberId());

			replaceUri += "?memberModifyAuthKey=" + authKeyForModify;

		} else if (replaceUri.equals("../member/leave")) {
			String authKeyForLeave = memberService.genAuthKey(rq.getLoginedMemberId());

			replaceUri += "?memberLeaveAuthKey=" + authKeyForLeave;
		}

		return rq.jsReplace("", replaceUri);
	}

	// 회원정보 수정 페이지 메서드
	@RequestMapping("/user/member/modify")
	public String showModify(String memberModifyAuthKey) {

		// 인증코드가 없으면 재접근 요청
		if (Ut.empty(memberModifyAuthKey)) {
			return rq.historyBackOnView("비밀번호 확인이 안되었습니다. 다시 시도해주세요.");
		}

		// 인증코드 확인
		ResultData checkMemberModifyAuthKeyRd = memberService.checkAuthKey(rq.getLoginedMemberId(),
				memberModifyAuthKey);

		if (checkMemberModifyAuthKeyRd.isFail()) {
			return rq.historyBackOnView(checkMemberModifyAuthKeyRd.getMsg());
		}

		return "user/member/modify";
	}

	// 회원 탈퇴 페이지 메서드
	@RequestMapping("/user/member/leave")
	public String showLeave(String memberLeaveAuthKey) {

		// 인증코드가 없으면 재접근 요청
		if (Ut.empty(memberLeaveAuthKey)) {
			return rq.historyBackOnView("비밀번호 확인이 안되었습니다. 다시 시도해주세요.");
		}

		// 인증코드 확인
		ResultData checkMemberLeaveAuthKeyRd = memberService.checkAuthKey(rq.getLoginedMemberId(), memberLeaveAuthKey);

		if (checkMemberLeaveAuthKeyRd.isFail()) {
			return rq.historyBackOnView(checkMemberLeaveAuthKeyRd.getMsg());
		}

		return "user/member/leave";
	}

	// 회원 수정하기 메서드
	@RequestMapping("/user/member/doModify")
	@ResponseBody
	public String doModify(String memberModifyAuthKey, String loginPw, String nickname, String cellphoneNo,
			String email) {

		// 인증코드가 없으면 재접근 요청
		if (Ut.empty(memberModifyAuthKey)) {
			return rq.jsHistoryBack("비밀번호 확인이 안되었습니다. 다시 시도해주세요.");
		}

		// 인증코드 확인
		ResultData checkMemberModifyAuthKeyRd = memberService.checkAuthKey(rq.getLoginedMemberId(),
				memberModifyAuthKey);

		if (checkMemberModifyAuthKeyRd.isFail()) {
			return rq.jsHistoryBack(checkMemberModifyAuthKeyRd.getMsg());
		}

		// 입력 데이터 유효성 검사
		if (Ut.empty(loginPw)) {
			loginPw = null;
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

		// 회원 수정하기
		memberService.doModify(rq.getLoginedMemberId(), loginPw, nickname, cellphoneNo, email);

		// 로그아웃
		rq.logout();

		return rq.jsReplace(Ut.f("%s 님의 회원정보가 수정되었습니다. 다시 로그인해주세요.", nickname), "/user/member/login");
	}

	// 회원 탈퇴하기 메서드
	@RequestMapping("/user/member/doLeave")
	@ResponseBody
	public String doLeave(String memberLeaveAuthKey) {

		// 인증코드가 없으면 재접근 요청
		if (Ut.empty(memberLeaveAuthKey)) {
			return rq.jsHistoryBack("비밀번호 확인이 안되었습니다. 다시 시도해주세요.");
		}

		// 인증코드 확인
		ResultData checkMemberLeaveAuthKeyRd = memberService.checkAuthKey(rq.getLoginedMemberId(), memberLeaveAuthKey);

		if (checkMemberLeaveAuthKeyRd.isFail()) {
			return rq.jsHistoryBack(checkMemberLeaveAuthKeyRd.getMsg());
		}

		// 회원 탈퇴하기
		memberService.doLeave(rq.getLoginedMemberId());

		// 로그아웃
		rq.logout();

		return rq.jsReplace("회원탈퇴가 완료되었습니다.", "/");
	}

	// 아이디 찾기 페이지 메서드
	@RequestMapping("/user/member/findLoginId")
	public String showFindLoginId() {

		return "user/member/findLoginId";
	}

	// 아이디 찾기 메서드
	@RequestMapping("/user/member/doFindLoginId")
	@ResponseBody
	public String doFindLoginId(String nickname, String email,
			@RequestParam(defaultValue = "/") String afterFindLoginIdUri) {

		// 로그인 확인
		if (rq.isLogined() == true) {
			return rq.jsHistoryBack("이미 로그인 중입니다.");
		}

		// 입력 데이터 유효성 검사
		if (Ut.empty(nickname)) {
			return rq.jsHistoryBack("닉네임(을)를 입력해주세요.");
		}

		if (Ut.empty(email)) {
			return rq.jsHistoryBack("이메일(을)를 입력해주세요.");
		}

		// 닉네임과 이메일로 회원정보 가져오기
		Member member = memberService.getMemberByNicknameAndEmail(nickname, email);

		// 찾기 실패시 uri가 중복되지 않도록 replace
		if (member == null) {
			return rq.jsReplace("등록되지 않은 회원이거나 잘못된 정보입니다.", rq.getFindLoginIdUri());
		}

		return rq.jsReplace(Ut.f("등록된 아이디 [%s] 입니다. 로그인 페이지로 이동합니다.", member.getLoginId()), "/user/member/login");
	}

	// 비밀번호 찾기 페이지 메서드
	@RequestMapping("/user/member/findLoginPw")
	public String showFindLoginPw() {

		return "user/member/findLoginPw";
	}

	// 비밀번호 찾기 메서드
	@RequestMapping("/user/member/doFindLoginPw")
	@ResponseBody
	public String doFindLoginPw(String loginId, String nickname, String email,
			@RequestParam(defaultValue = "/") String afterFindLoginPwUri) {

		// 로그인 확인
		if (rq.isLogined() == true) {
			return rq.jsHistoryBack("이미 로그인 중입니다.");
		}

		// 입력 데이터 유효성 검사
		if (Ut.empty(loginId)) {
			return rq.jsHistoryBack("아이디(을)를 입력해주세요.");
		}

		if (Ut.empty(email)) {
			return rq.jsHistoryBack("이메일(을)를 입력해주세요.");
		}

		// 아이디로 회원정보 가져오기
		Member member = memberService.getMemberByLoginId(loginId);

		// 찾기 실패시 uri가 중복되지 않도록 replace
		if (member == null) {
			return rq.jsReplace("등록되지 않은 회원이거나 잘못된 아이디입니다.", rq.getFindLoginPwUri());
		}

		if (member.getEmail().equals(email) == false) {
			return rq.jsReplace("등록되지 않은 회원이거나 잘못된 이메일입니다.", rq.getFindLoginPwUri());
		}

		// 임시 비밀번호를 이메일로 발송
		ResultData notifyTempLoginPwByEmailRs = memberService.notifyTempLoginPwByEmail(member);

		return rq.jsReplace(notifyTempLoginPwByEmailRs.getMsg(), afterFindLoginPwUri);
	}

	// 입력아이디 사용중인지 확인
	@RequestMapping("/user/member/getLoginIdDup")
	@ResponseBody
	public ResultData getLoginIdDup(String loginId) {

		if (Ut.empty(loginId)) {
			return ResultData.from("F-1", "아이디를 입력해주세요.");
		}

		Member oldMember = memberService.getMemberByLoginId(loginId);

		if (oldMember != null) {
			return ResultData.from("F-A", "해당 아이디는 이미 사용중입니다.", "loginId", loginId);
		}

		return ResultData.from("S-A", "사용가능한 로그인아이디 입니다.", "loginId", loginId);
	}

	// 입력 닉네임과 이메일 사용중인지 확인
	@RequestMapping("/user/member/getNicknameAndEmailDup")
	@ResponseBody
	public ResultData getNicknameAndEmailDup(String nickname, String email) {

		if (Ut.empty(nickname)) {
			return ResultData.from("F-1", "닉네임을 입력해주세요.");
		}

		if (Ut.empty(email)) {
			return ResultData.from("F-2", "이메일을 입력해주세요.");
		}

		Member oldMember = memberService.getMemberByNicknameAndEmail(nickname, email);

		if (oldMember != null) {
			return ResultData.from("F-A", "해당 닉네임과 이메일은 이미 등록되었습니다.", nickname, email);
		}

		return ResultData.from("S-A", "사용가능한 닉네임과 이메일 입니다.", nickname, email);
	}
}
