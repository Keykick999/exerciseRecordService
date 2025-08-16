//package com.example.fitnessWeb;
//
//import com.example.fitnessWeb.controller.ChatController;
//import com.example.fitnessWeb.dto.*;
//import com.example.fitnessWeb.entity.Role;
//import com.example.fitnessWeb.service.ChatService;
//import com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper;
//import com.epages.restdocs.apispec.ResourceSnippetParameters;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.mockito.Mockito;
//import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
//import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
//import org.springframework.boot.test.mock.mockito.MockBean;
//import org.springframework.data.domain.*;
//import org.springframework.http.MediaType;
//import org.springframework.restdocs.payload.FieldDescriptor;
//import org.springframework.test.web.servlet.MockMvc;
//import org.springframework.beans.factory.annotation.Autowired;
//import java.time.LocalDateTime;
//import java.util.List;
//import static org.mockito.ArgumentMatchers.*;
//import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
//import static com.epages.restdocs.apispec.ResourceDocumentation.resource;
//import static org.springframework.restdocs.payload.PayloadDocumentation.*;
//import static org.springframework.restdocs.request.RequestDocumentation.*;
//
//@WebMvcTest(controllers = ChatController.class)
//@AutoConfigureRestDocs(outputDir = "build/generated-snippets")
//class ChatControllerDocsTest {
//
//    @Autowired MockMvc mockMvc;
//    @MockBean ChatService chatService;
//
//    // 공통: Spring Data Page 메타 필드
//    private static FieldDescriptor[] pageMeta() {
//        return new FieldDescriptor[] {
//                fieldWithPath("content").description("페이지 컨텐츠 배열"),
//                fieldWithPath("pageable").ignored(),
//                fieldWithPath("last").description("마지막 페이지 여부"),
//                fieldWithPath("totalPages").description("전체 페이지 수"),
//                fieldWithPath("totalElements").description("전체 요소 수"),
//                fieldWithPath("size").description("페이지 크기"),
//                fieldWithPath("number").description("현재 페이지 번호(0-base)"),
//                fieldWithPath("sort").ignored(),
//                fieldWithPath("first").description("첫 페이지 여부"),
//                fieldWithPath("numberOfElements").description("현재 페이지 요소 수"),
//                fieldWithPath("empty").description("비어있는지 여부")
//        };
//    }
//
//    @Test
//    @DisplayName("POST /api/chat : 새 채팅 시작 및 질문")
//    void askCoach() throws Exception {
//        Mockito.when(chatService.askModel(eq("벤치프레스 어떻게 해?")))
//                .thenReturn(new ChatResponse(100L, "어깨를 고정하고..."));
//
//        String body = """
//            {"message":"벤치프레스 어떻게 해?"}
//        """;
//
//        mockMvc.perform(post("/api/chat")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(body))
//                .andExpect(status().isOk())
//                .andDo(MockMvcRestDocumentationWrapper.document("chat-ask",
//                        resource(ResourceSnippetParameters.builder()
//                                .tag("Chat")
//                                .summary("새 채팅 시작 및 첫 질문")
//                                .requestFields(
//                                        fieldWithPath("message").description("사용자 질문 텍스트")
//                                )
//                                .responseFields(
//                                        fieldWithPath("chatId").description("생성된 채팅 ID"),
//                                        fieldWithPath("reply").description("AI 코치의 응답")
//                                )
//                                .build()
//                        )
//                ));
//    }
//
//    @Test
//    @DisplayName("POST /api/chat/{chatId}/message : 기존 채팅에 질문 추가")
//    void continueChat() throws Exception {
//        Mockito.when(chatService.continueChat(eq(100L), eq("그립 간격은?")))
//                .thenReturn(new ChatResponse(100L, "바벨의 눈금 기준..."));
//
//        String body = """
//            {"message":"그립 간격은?"}
//        """;
//
//        mockMvc.perform(post("/api/chat/{chatId}/message", 100)
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(body))
//                .andExpect(status().isOk())
//                .andDo(MockMvcRestDocumentationWrapper.document("chat-continue",
//                        resource(ResourceSnippetParameters.builder()
//                                .tag("Chat")
//                                .summary("대화 이어가기")
//                                .pathParameters(
//                                        parameterWithName("chatId").description("채팅 ID")
//                                )
//                                .requestFields(
//                                        fieldWithPath("message").description("추가 질문 텍스트")
//                                )
//                                .responseFields(
//                                        fieldWithPath("chatId").description("채팅 ID"),
//                                        fieldWithPath("reply").description("AI 코치의 응답")
//                                )
//                                .build()
//                        )
//                ));
//    }
//
//    @Test
//    @DisplayName("GET /api/chat/{chatId}/messages?page=0 : 특정 채팅의 메시지 페이징 조회")
//    void getChatMessages() throws Exception {
//        List<ChatMessageDto> list = List.of(
//                ChatMessageDto.builder()
//                        .id(1L).role(Role.USER).content("벤치 알려줘")
//                        .createdAt(LocalDateTime.now().minusMinutes(2)).orderNo(1).build(),
//                ChatMessageDto.builder()
//                        .id(2L).role(Role.ASSISTANT).content("어깨 고정...")
//                        .createdAt(LocalDateTime.now().minusMinutes(1)).orderNo(2).build()
//        );
//        Page<ChatMessageDto> page = new PageImpl<>(list, PageRequest.of(0, 20, Sort.by("orderNo")), 2);
//        Mockito.when(chatService.getChatMessages(eq(100L), eq(0))).thenReturn(page);
//
//        mockMvc.perform(get("/api/chat/{chatId}/messages", 100)
//                        .param("page", "0"))
//                .andExpect(status().isOk())
//                .andDo(MockMvcRestDocumentationWrapper.document("chat-messages",
//                        resource(ResourceSnippetParameters.builder()
//                                .tag("Chat")
//                                .summary("대화 메시지 목록 조회(페이징)")
//                                .pathParameters(
//                                        parameterWithName("chatId").description("채팅 ID")
//                                )
//                                .requestParameters(
//                                        parameterWithName("page").optional().description("페이지 번호(0-base, 기본값 0)")
//                                )
//                                .responseFields(
//                                        fieldWithPath("content[].id").description("메시지 ID"),
//                                        fieldWithPath("content[].role").description("메시지 역할(USER/ASSISTANT 등)"),
//                                        fieldWithPath("content[].content").description("메시지 내용"),
//                                        fieldWithPath("content[].createdAt").description("작성 시각(UTC 또는 서버 로컬)"),
//                                        fieldWithPath("content[].orderNo").description("대화 내 표시 순서")
//                                ).and(pageMeta())
//                                .build()
//                        )
//                ));
//    }
//
//    @Test
//    @DisplayName("GET /api/chat?page=0 : 사용자의 채팅 목록(페이징)")
//    void getChats() throws Exception {
//        List<ChatDto> list = List.of(
//                new ChatDto(100L, "가슴 루틴 문의", LocalDateTime.now().minusHours(2), LocalDateTime.now()),
//                new ChatDto(101L, "등 운동", LocalDateTime.now().minusDays(1), LocalDateTime.now().minusHours(1))
//        );
//        Page<ChatDto> page = new PageImpl<>(list, PageRequest.of(0, 20), 2);
//        Mockito.when(chatService.getChats(eq(0))).thenReturn(page);
//
//        mockMvc.perform(get("/api/chat").param("page", "0"))
//                .andExpect(status().isOk())
//                .andDo(MockMvcRestDocumentationWrapper.document("chat-list",
//                        resource(ResourceSnippetParameters.builder()
//                                .tag("Chat")
//                                .summary("채팅 목록 조회(페이징)")
//                                .requestParameters(
//                                        parameterWithName("page").optional().description("페이지 번호(0-base, 기본값 0)")
//                                )
//                                .responseFields(
//                                        fieldWithPath("content[].id").description("채팅 ID"),
//                                        fieldWithPath("content[].title").description("채팅 제목(첫 질문 요약 등)"),
//                                        fieldWithPath("content[].createdAt").description("채팅 생성 시각"),
//                                        fieldWithPath("content[].lastAccessedAt").description("마지막 접근 시각")
//                                ).and(pageMeta())
//                                .build()
//                        )
//                ));
//    }
//
//    @Test
//    @DisplayName("DELETE /api/chat/{chatId} : 채팅 삭제 (204)")
//    void deleteChat() throws Exception {
//        mockMvc.perform(delete("/api/chat/{chatId}", 100))
//                .andExpect(status().isNoContent())
//                .andDo(MockMvcRestDocumentationWrapper.document("chat-delete",
//                        resource(ResourceSnippetParameters.builder()
//                                .tag("Chat")
//                                .summary("채팅 삭제")
//                                .pathParameters(
//                                        parameterWithName("chatId").description("채팅 ID")
//                                )
//                                .build()
//                        )
//                ));
//    }
//}