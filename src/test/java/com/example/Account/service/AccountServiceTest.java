package com.example.Account.service;

import com.example.Account.Exception.AccountException;
import com.example.Account.domain.Account;
import com.example.Account.domain.AccountUser;
import com.example.Account.dto.AccountDto;
import com.example.Account.repository.AccountUserRepository;
import com.example.Account.type.AccountStatus;
import com.example.Account.repository.AccountRepository;
import com.example.Account.type.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {
    @Mock
    private AccountRepository accountRepository;

    @Mock
    private AccountUserRepository accountUserRepository;

    @InjectMocks
    private AccountService accountService;

    @Test
    void createAccountSuccess(){
        AccountUser user = AccountUser.builder().id(12L).name("Pobi").build();

        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.of(user));

        given(accountRepository.findFirstByOrderByIdDesc())
                .willReturn(Optional.of(Account.builder()
                        .accountNumber("1000000012").build()));

        given(accountRepository.save(any()))
                .willReturn(Account.builder()
                        .accountUser(user)
                        .accountNumber("1000000013").build());

        ArgumentCaptor<Account> captor = ArgumentCaptor.forClass(Account.class);

        AccountDto accountDto = accountService.createAccount(1L, 1000L);

        verify(accountRepository, times(1)).save(captor.capture());
        assertEquals(12L, accountDto.getUserId());
        assertEquals("1000000013", accountDto.getAccountNumber());
    }

    @Test
    void createFirstAccountSuccess(){
        AccountUser user = AccountUser.builder().id(15L).name("Pobi").build();

        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.of(user));

        given(accountRepository.findFirstByOrderByIdDesc())
                .willReturn(Optional.empty());

        given(accountRepository.save(any()))
                .willReturn(Account.builder()
                        .accountUser(user)
                        .accountNumber("1000000015").build());

        ArgumentCaptor<Account> captor = ArgumentCaptor.forClass(Account.class);

        AccountDto accountDto = accountService.createAccount(1L, 1000L);

        verify(accountRepository, times(1)).save(captor.capture());
        assertEquals(15L, accountDto.getUserId());
        assertEquals("1000000000", accountDto.getAccountNumber());
    }

    @Test
    @DisplayName("해당 유저 없음 - 계좌 생성 실패")
    void createAccount_UserNotFound(){
        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.empty());

        AccountException accountException = assertThrows(AccountException.class, ()-> accountService.createAccount(1L, 1000L));

        assertEquals(ErrorCode.USER_NOT_FOUND, accountException.getErrorCode());
    }

    @Test
    @DisplayName("유저당 최대 거래는 10개")
    void createAccount_maxAccountIs10(){
        AccountUser user = AccountUser.builder().id(15L).name("Pobi").build();

        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.of(user));
        given(accountRepository.countByAccountUser((any())))
                .willReturn(10);

        AccountException accountException = assertThrows(AccountException.class, ()-> accountService.createAccount(1L, 1000L));

        assertEquals(ErrorCode.MAX_ACCOUNT_PER_USER_10, accountException.getErrorCode());

    }

    @Test
    @DisplayName("계좌 조회 성공")
    void testGetAccount() {
        //given
        given(accountRepository.findById(anyLong()))
                .willReturn(Optional.of(Account.builder()
                        .accountStatus(AccountStatus.UNREGISTERED)
                        .accountNumber("65789")
                                .build()));

        //when
        Account account = accountService.getAccount(12345L);

        //then
        assertEquals("65789", account.getAccountNumber());
        assertEquals(AccountStatus.UNREGISTERED, account.getAccountStatus());
    }
}