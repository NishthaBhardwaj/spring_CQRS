package com.techbank.account.cmd.domain;

import com.techbank.account.cmd.api.commands.OpenAccountCommand;
import com.techbank.account.common.events.AccountClosedEvent;
import com.techbank.account.common.events.AccountOpenedEvent;
import com.techbank.account.common.events.FundDepositedEvent;
import com.techbank.account.common.events.FundsWithdrawnEvent;
import com.techbank.cqrs.core.domain.AggregateRoot;
import lombok.NoArgsConstructor;

import java.util.Date;

@NoArgsConstructor
public class AccountAggregate extends AggregateRoot {
    private Boolean active;
    private double balance;

    public AccountAggregate(OpenAccountCommand command){
        AccountOpenedEvent accountOpenedEvent = new AccountOpenedEvent();
        accountOpenedEvent.setId(command.getId());
        accountOpenedEvent.setAccountHolder(command.getAccountHolder());
        accountOpenedEvent.setOpeningBalance(command.getOpeningBalance());
        accountOpenedEvent.setCreatedDate(new Date());
        accountOpenedEvent.setAccountType(command.getAccountType());
        raiseEvent(accountOpenedEvent);
    }

    public void apply(AccountOpenedEvent event){
        this.id = event.getId();
        this.active = true;
        this.balance = event.getOpeningBalance();
    }

    public void depositFunds(double amount){
        if(!this.active){
            throw new IllegalStateException("Funds can not be deposited into a closed account");
        }
        if(amount <=0){
            throw new IllegalStateException("The deposit amount must be greater than 0");
        }
        FundDepositedEvent fundDepositedEvent = new FundDepositedEvent();
        fundDepositedEvent.setId(this.id);
        fundDepositedEvent.setAmount(amount);

        raiseEvent(fundDepositedEvent);
    }

    public void apply(FundDepositedEvent event){
        this.id = event.getId();
        this.balance += event.getAmount();
    }

    public void withdrawFunds(double amount){
        if(!this.active){
            throw new IllegalStateException("Funds can not be withdrawn into a closed account");
        }
        FundsWithdrawnEvent fundsWithdrawnEvent = new FundsWithdrawnEvent();
        fundsWithdrawnEvent.setId(this.id);
        fundsWithdrawnEvent.setAmount(amount);
        raiseEvent(fundsWithdrawnEvent);

    }
    public void apply(FundsWithdrawnEvent event){
        this.id = event.getId();
        this.balance -= event.getAmount();
    }

    public void closeAccount(){
        if(!this.active){
            throw new IllegalStateException("The bank account has already been closed");
        }
        AccountClosedEvent accountClosedEvent = new AccountClosedEvent();
        accountClosedEvent.setId(this.id);
        raiseEvent(accountClosedEvent);
    }

    public void apply(AccountClosedEvent event){
        this.id = event.getId();
        this.active = false;

    }
}
