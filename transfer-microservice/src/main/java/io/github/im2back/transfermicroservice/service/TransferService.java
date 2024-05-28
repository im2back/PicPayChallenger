package io.github.im2back.transfermicroservice.service;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.github.im2back.transfermicroservice.clienthttp.ClientResourceClient;
import io.github.im2back.transfermicroservice.dto.TransferRequestDto;
import io.github.im2back.transfermicroservice.dto.UserDto;
import io.github.im2back.transfermicroservice.service.util.NotificationRequestDto;
import io.github.im2back.transfermicroservice.validation.transfer.TransferValidations;

@Service
public class TransferService {

	@Autowired
	private List<TransferValidations> transferValidations;

	@Autowired
	private AuthorizationService authorizationService;

	@Autowired
	private NotificationService notificationService;

	@Autowired
	ClientResourceClient clientResourceClient;

	@Transactional
	public void transfer(Long idPayer, Long idPayee, BigDecimal value) {

		// validacoes de usuario
		transferValidations.forEach(valid -> valid.valid(idPayer, idPayee, value));

		// autorizador externo
		authorizationService.finalizeTransfer();

		// transferencia
		receivePayment(idPayer, idPayee, value);

	}

	public void receivePayment(Long idPayer, Long idPayee, BigDecimal value) {
		var response = clientResourceClient.findUser(idPayee);
		UserDto userPayee = response.getBody();

		// requisicao enviando os ids e o valor para serem persistidos
		clientResourceClient.transfer(new TransferRequestDto(idPayer, idPayee, value));

		// Enviar notificação
		notificationService
				.sendNotification(new NotificationRequestDto(userPayee.email(), "Pagamento recebido com sucesso!"));
	}

}