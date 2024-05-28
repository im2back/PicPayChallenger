package io.github.im2back.usermicroservice.service;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.github.im2back.usermicroservice.model.dto.TransferRequestDto;
import io.github.im2back.usermicroservice.model.dto.UserRegisterRequestDto;
import io.github.im2back.usermicroservice.model.dto.UserRegisterResponseDto;
import io.github.im2back.usermicroservice.model.entities.user.User;
import io.github.im2back.usermicroservice.model.entities.wallet.Wallet;
import io.github.im2back.usermicroservice.repositories.UserRepository;
import io.github.im2back.usermicroservice.service.exceptions.UserNotFoundException;
import io.github.im2back.usermicroservice.validation.user.UserRegistrationValidation;

@Service
public class UserService {

	@Autowired
	private UserRepository repository;

	@Autowired
	private List<UserRegistrationValidation> userRegistrationValidation;

	@Transactional
	public UserRegisterResponseDto saveUser(UserRegisterRequestDto dto) {
		userRegistrationValidation.forEach(valid -> valid.valid(dto));

		// Criação da Wallet
		Wallet wallet = new Wallet();
		wallet.setBalance(BigDecimal.ZERO);

		// Criação do User e associação da Wallet
		User user = new User(dto.fullName(), dto.identificationDocument(), dto.email(), dto.password(), dto.type(),
				wallet);
		wallet.setUser(user);

		// Persistência do User, o que também deve persistir a Wallet devido ao cascata
		User savedUser = repository.save(user);

		// Construção do DTO de resposta
		return new UserRegisterResponseDto(savedUser.getId(), savedUser.getFullName(),
				savedUser.getIdentificationDocument(), savedUser.getEmail(), savedUser.getPassword(),
				savedUser.getType(), savedUser.getWallet().getBalance());

	}

	@Transactional
	public void transfer(TransferRequestDto dto) {

		// Carreguei os usuarios envolvidos e transferindo
		User userPayer = findById(dto.idPayer());
		User userPayee = findById(dto.idPayee());

		userPayer.getWallet().transfer(dto.value());
		userPayee.getWallet().receiveTransfer(dto.value());

		// persistindo a transferencia
		repository.saveAll(Arrays.asList(userPayee, userPayer));
	}

	@Transactional(readOnly = true)
	public User findById(Long id) {
		return repository.findById(id).orElseThrow(() -> new UserNotFoundException(id));
	}

	@Transactional(readOnly = true)
	public Optional<User> findByDocument(String document) {
		return repository.findByIdentificationDocument(document);
	}

	@Transactional(readOnly = true)
	public Optional<User> findByEmail(String Email) {
		return repository.findByEmail(Email);
	}

}