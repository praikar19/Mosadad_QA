package com.mosadad.testing.pages.claims;

import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import com.mosadad.testing.pages.BasePage;

public class HomePage extends BasePage {

    public HomePage(Page page) {
        super(page);
    }

    private static final String MY_WALLET = "My Wallets";

    public WalletPage clickMyWallet(){
        //page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName(MY_WALLET)).click();
        click(MY_WALLET);
        return new WalletPage(page);
    }





}
