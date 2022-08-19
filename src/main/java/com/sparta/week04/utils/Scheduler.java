package com.sparta.week04.utils;

import com.sparta.week04.models.ItemDto;
import com.sparta.week04.models.Product;
import com.sparta.week04.models.ProductRepository;
import com.sparta.week04.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor // final 멤버 변수를 자동으로 생성합니다.
@Component // 스프링이 필요 시 자동으로 생성하는 클래스 목록에 추가합니다.
public class Scheduler {

    private final ProductRepository productRepository;
    private final ProductService productService;
    private final NaverShopSearch naverShopSearch;

    //cron 이란 스케률링 매번 정해진 시간에 맞춰서 작동하는 '예약'같은거
    // 초, 분, 시, 일, 월, 주 순서
    // *은 뭐든지 상관이 없다 만일 ******으로 되있을 경우 매초 실행
    //따라서 아래 내용은, 일월주는 상관없고, 시간이 새벽 한시, 1시 0분 0초일때 실행
    // 시는 0부터 23까지 가능
    // 만일 **1**로 할경우, 1시 0분 0초부터 1시 59분 59초까지 => 1시범위내에 매초마다 실행
    @Scheduled(cron = "0 0 1 * * *")
    //스케쥴링을 하다보면 오류가 발생할 수 도 있는데, 스프링에게 대처하는 법 알려줌
    public void updatePrice() throws InterruptedException {
        System.out.println("가격 업데이트 실행");
        // 저장된 모든 관심상품을 조회합니다.
        List<Product> productList = productRepository.findAll();
        for (int i=0; i<productList.size(); i++) {
            // 1초에 한 상품 씩 조회합니다 (Naver 제한)
            //TimeUnit기준으로 초마다 1초간 자라,쉬어라
            //아래 코드가 없으면 전체적인 코드 자체는 문제가 없는데,
            // 네이버에서 너무 짧은 시간에 요청이 자주오면 막아버리기때문에 넣어줌
            TimeUnit.SECONDS.sleep(1);
            // i 번째 관심 상품을 꺼냅니다.
            Product p = productList.get(i);
            // i 번째 관심 상품의 제목으로 검색을 실행합니다.
            String title = p.getTitle();
            String resultString = naverShopSearch.search(title);
            // i 번째 관심 상품의 검색 결과 목록 중에서 첫 번째 결과를 꺼냅니다.
            List<ItemDto> itemDtoList = naverShopSearch.fromJSONtoItems(resultString);
            //제목으로 검색된 여러가지 아이템이 나올텐데 당연히 similarity(유사성)로 검색했으니까
            //제일 위에 있는  0번째가 내가 원하는 title일 것이다
            //내가 원하는 정보를 담고있는 Dto로
            ItemDto itemDto = itemDtoList.get(0);
            // i 번째 관심 상품 정보를 업데이트합니다.
            Long id = p.getId();
            productService.updateBySearch(id, itemDto);
        }
    }
}