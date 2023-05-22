package com.example.Account.service;

import com.example.Account.Exception.AccountException;
import com.example.Account.domain.Account;
import com.example.Account.domain.AccountUser;
import com.example.Account.dto.AccountDto;
import com.example.Account.repository.AccountRepository;
import com.example.Account.repository.AccountUserRepository;
import com.example.Account.type.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static com.example.Account.type.AccountStatus.IN_USE;

@Service
@RequiredArgsConstructor
public class AccountService {
    private final AccountRepository accountRepository;
    private final AccountUserRepository accountUserRepository;

    @Transactional
    public AccountDto createAccount(Long userId, Long initialBalance){
        AccountUser accountUser =  accountUserRepository.findById(userId)
                .orElseThrow(() -> new AccountException(ErrorCode.USER_NOT_FOUND));

        if(accountRepository.countByAccountUser(accountUser) == 10){
            throw new AccountException(ErrorCode.MAX_ACCOUNT_PER_USER_10);
        }

        String newAccountNumber = accountRepository.findFirstByOrderByIdDesc()
                .map(account -> (Integer.parseInt(account.getAccountNumber()))+1 + "")
                .orElse("1000000000");


        return AccountDto.fromEntity(
                accountRepository.save(
                        Account.builder()
                        .accountUser(accountUser)
                        .accountStatus(IN_USE)
                        .accountNumber(newAccountNumber)
                        .balance(initialBalance)
                        .registeredAt(LocalDateTime.now())
                        .build()
        ));
    }

    @Transactional
    public Account getAccount(Long id){
        return accountRepository.findById(id).get();
    }
}
