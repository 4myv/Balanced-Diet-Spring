package com.balanceddiet.server.exception;

// 찾는 데이터가 없을 때 던지는 에러 -> 404
public class NotFoundException extends RuntimeException {
    // RuntimeException(런타임 에러)을 상속받는다 -> 언체크 예외(unchecked exception)가 됨
    // 왜 Exception이 아니라 RuntimeException을 물려받냐면,
    // Exception을 바로 상속받으면 체크 예외(checked exception)가 되어,
    // 이 에러를 던지는 메서드를 부르는 모든 곳에서 컴파일러가 try/catch(또는 throws)를 강제한다
    // IllegalArgumentException도 RuntimeException의 자식이라 try/catch 없이 던질 수 있었음

    // 생성자
    public NotFoundException(String message) {
        super(message);
        // message파라미터를 담아 부모 생성자 호출
    }
}
