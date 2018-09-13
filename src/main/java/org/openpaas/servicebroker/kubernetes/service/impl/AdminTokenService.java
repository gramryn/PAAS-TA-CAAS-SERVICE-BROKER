package org.openpaas.servicebroker.kubernetes.service.impl;

import java.io.IOException;

import org.openpaas.servicebroker.kubernetes.repo.JpaAdminTokenRepository;
import org.openpaas.servicebroker.kubernetes.service.PropertyService;
import org.openpaas.servicebroker.kubernetes.service.RestTemplateService;
import org.openpaas.servicebroker.kubernetes.service.SshService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Admin token에 관한 서비스 
 * @author Hyerin
 * @since 2018.08.22
 * @version 20180822
 */
@Service
public class AdminTokenService {
	
	private static final Logger logger = LoggerFactory.getLogger(AdminTokenService.class);

	@Autowired
	JpaAdminTokenRepository adminTokenRepository;
	
	@Autowired
	SshService sshService;
	
	@Autowired
	PropertyService propertyService;	
	
	@Autowired
	RestTemplateService restTemplateService;
	
	/**
	 * broker DB에 token이 없을 경우, ssh통신으로 set-context 명령어 호출 
	 * 반환값음 없다.
	 * @author Hyerin
	 * @since 2018.08.22
	 * @version 20180822
	 */
	public void setContext() {
		logger.info("execute ssh command to caas master server to set admin token");
		sshService.executeSsh(propertyService.getCaasClusterCommand());
		String[] cmd = new String[1];
		cmd[0] = "sh";
		cmd[0] = propertyService.getCaasClusterCommand();
		
		try {
			Process p = Runtime.getRuntime().exec(cmd);
		} catch (IOException e) {
			logger.error("Something Wrong!!");
			e.printStackTrace();
		}
	}
	
	private boolean tokenExist() {
		return adminTokenRepository.exists(propertyService.getAdminToken());
	}
	
	public boolean tokenValidation() {
		return restTemplateService.tokenValidation();
	}
	
	/**
	 * 토큰의 존재유무, 값의 확인을 위한 함수
	 * @author Hyerin
	 * @since 2018.08.22
	 * @version 20180822
	 */
	public void checkToken() {
		logger.info("token check");
		
		// 토큰이 존재하지 않을 때
		if(!tokenExist()) {
			logger.info("does not exist token, So set admin token");
			setContext();
			return;
		}
		
		// 토큰이 존재하고, 갱신이 필요할 때
		if(!tokenValidation()) {
			setContext();
			return;
		}
			
	}
}
