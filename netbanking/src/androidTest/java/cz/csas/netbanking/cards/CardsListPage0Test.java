package cz.csas.netbanking.cards;

import org.junit.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import cz.csas.cscore.client.rest.CallbackWebApi;
import cz.csas.cscore.error.CsSDKError;
import cz.csas.cscore.judge.Constants;
import cz.csas.cscore.judge.JudgeUtils;
import cz.csas.cscore.utils.TimeUtils;
import cz.csas.cscore.webapi.Pagination;
import cz.csas.netbanking.AccountNumber;
import cz.csas.netbanking.NetbankingTest;

import static junit.framework.Assert.assertEquals;

/**
 * @author Frantisek Kratochvil <kratochvilf@gmail.com>
 * @since 14.04.16.
 */
public class CardsListPage0Test extends NetbankingTest {

    private final String X_JUDGE_CASE = "cards.list.page0";
    private CountDownLatch mCardsSignal;
    private CardsListResponse mCardsListResponse;

    @Override
    public void setUp() {
        super.setUp();
        mCardsSignal = new CountDownLatch(1);
        JudgeUtils.setJudge(mJudgeClient, X_JUDGE_CASE, mXJudgeSessionHeader);
        Map<String, String> headers = new HashMap<>();
        headers.put(Constants.XJUDGE_SESSION_HEADER_NAME, mXJudgeSessionHeader);
        mNetbankingClient.setGlobalHeaders(headers);
    }

    @Test
    public void testCardsListPage0() {
        CardsParameters parameters = new CardsParameters(new Pagination(0,1), null);

        mNetbankingClient.getCardsResource().list(parameters, new CallbackWebApi<CardsListResponse>() {
            @Override
            public void success(CardsListResponse cardsListResponse) {
                mCardsListResponse = cardsListResponse;
                mCardsSignal.countDown();
            }

            @Override
            public void failure(CsSDKError error) {
                mCardsSignal.countDown();
            }
        });

        try {
            mCardsSignal.await(20, TimeUnit.SECONDS); // wait for callback
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        assertEquals(Long.valueOf(0), mCardsListResponse.getPageNumber());
        assertEquals(Long.valueOf(2), mCardsListResponse.getPageCount());
        assertEquals(Long.valueOf(1), mCardsListResponse.getPageSize());
        assertEquals(Long.valueOf(1), mCardsListResponse.getNextPage());

        List<Card> cards = mCardsListResponse.getCards();
        assertEquals(1, cards.size());

        Card card = cards.get(0);
        CardMainAccount account = card.getMainAccount();
        AccountNumber accountNumber = account.getAccountno();

        assertEquals("A705433CFCD205249F4B816F2C63D309AEEFF4C9", card.getId());
        assertEquals("VRBA ALEŠ", card.getOwner());
        assertEquals("451161XXXXXX7982", card.getNumber());
        assertEquals("3", card.getSequenceNumber());
        assertEquals("4511611", card.getProductCode());
        assertEquals(TimeUtils.getPlainDate("2017-11-30"), card.getExpiryDate());
        assertEquals(CardState.TEMPORARY_BLOCKED, card.getState());
        assertEquals(CardType.BANK_CARD, card.getType());
        assertEquals(CardProvider.ERSTE_BANK, card.getProvider());
        assertEquals(TimeUtils.getPlainDate("2014-12-01"), card.getValidFromDate());
        assertEquals(CardCharacteristic.MAIN, card.getCharacteristic());

        assertEquals("4B2F9EBE742BCAE1E98A78E12F6FBC62464A74EE", account.getId());
        assertEquals("Aleš Vrba", account.getHolderName());
        assertEquals("2059930033", accountNumber.getNumber());
        assertEquals("0800", accountNumber.getBankCode());
        assertEquals("CZ", accountNumber.getCountryCode());
        assertEquals("CZ1208000000002059930033", accountNumber.getCzIban());
        assertEquals("GIBACZPX", accountNumber.getCzBic());

        assertEquals(CardDeliveryMode.HOME, card.getCardDeliveryMode());
        assertEquals(Arrays.asList(
                "automaticReplacementOn",
                "activationAllowed",
                "contactlessEnabled"), card.getFlags());
        assertEquals(Arrays.asList(
                "contactless",
                "replacementCard",
                "secureOnlineShopping"), card.getFeatures());
        assertEquals("moje karta", card.getAlias());
        assertEquals(LockReason.LOSS, card.getLockReason());
        assertEquals("Visa Classic debetní - Partner", card.getProductI18N());

    }
}
