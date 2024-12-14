package apihive.config.strategy;

import apihive.exposer.ExposeResponse;
import java.util.List;
import java.util.Map;

public interface InterfaceDiscoveryStrategy {

  /**
   * 서비스 탐색 및 인터페이스 생성 전략을 정의합니다.
   *
   * @return 생성된 인터페이스 클래스 목록
   */
  List<Class<?>> discoverAndCreateInterfaces();

  /**
   * 특정 서비스에 대한 인터페이스를 생성합니다.
   *
   * @param serviceName 서비스 이름
   * @param serviceUrl  서비스 URL
   * @return 생성된 인터페이스 클래스
   */
  Class<?> createInterfaceForService(String serviceName, String serviceUrl);

  /**
   * ExposeResponse로부터 직접 인터페이스를 생성합니다.
   *
   * @param serviceName 서비스 이름
   * @param response    컨트롤러 메타데이터
   * @return 생성된 인터페이스 클래스
   */
  Class<?> createInterfaceFromMetadata(String serviceName, ExposeResponse response);
} 