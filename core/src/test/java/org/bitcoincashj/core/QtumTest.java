package org.bitcoincashj.core;

/**
 * Created by wangh09 on 2018/2/23.
 */
import org.bitcoincashj.crypto.TransactionSignature;
import org.bitcoincashj.params.QtumMainNetParams;
import org.bitcoincashj.params.RegTestParams;
import org.bitcoincashj.script.Script;
import org.bitcoincashj.script.ScriptBuilder;
import org.bitcoincashj.wallet.SendRequest;
import org.junit.Test;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A draft of test cases for Bitcoin Cash signature
 */
public class QtumTest {

    private static final NetworkParameters PARAMS = QtumMainNetParams.get();
/*
    @Test
    public void testP2SHMultiSign() {
        ECKey key1 = DumpedPrivateKey.fromBase58(PARAMS, "cUfapEEhg2y9a7U135VVEe5fz52Zhf1Sytu4voet527PcUvTDw62").getKey();
        ECKey key2 = DumpedPrivateKey.fromBase58(PARAMS, "cReifGpzVzS1LynpwMpoMxeF537cs3MhSxznWysiRtAemJgkgQnF").getKey();

        Address destAddress = Address.fromBase58(PARAMS, "mhwQAhYxou8Z1nfVNeJQmopPvfaoCTYa7N");

        List<ECKey> keys = new ArrayList<ECKey>();
        keys.add(key1);
        keys.add(key2);

        Collections.sort(keys, ECKey.PUBKEY_COMPARATOR);

        Script scriptPubKey = ScriptBuilder.createP2SHOutputScript(2, keys);
        Address p2shAddress = Address.fromP2SHScript(PARAMS, scriptPubKey);

        System.out.println(p2shAddress.toBase58());

        Transaction tx = new Transaction(PARAMS);
        Script redeemScript = ScriptBuilder.createRedeemScript(2, keys);

        tx.addInput(new Sha256Hash("a7686b3919526a6e733ccd1a2413f517ae44bb01a31cb4d2caaa173dc69b6b85"), 1, scriptPubKey, Coin.valueOf(100000000L));
        tx.addOutput(Coin.valueOf(0, 99), destAddress);


        List<TransactionSignature> signatures = new ArrayList<TransactionSignature>();
        for (ECKey key : keys) {
            Sha256Hash hash = tx.hashForSignature(0, redeemScript, Transaction.SigHash.ALL, false);
            ECKey.ECDSASignature sig = key.sign(hash);
            TransactionSignature signature = new TransactionSignature(sig, Transaction.SigHash.ALL, false, true);
            signatures.add(signature);
        }

        Script scriptSig = ScriptBuilder.createP2SHMultiSigInputScript(signatures, redeemScript);
        tx.getInput(0).setScriptSig(scriptSig);

        System.out.println(Utils.HEX.encode(tx.bitcoinSerialize()));

        scriptSig = ScriptBuilder.createP2SHMultiSigInputScript(null, redeemScript);

        for (int i = 0; i < keys.size(); i++) {
            ECKey key = keys.get(i);
            Sha256Hash hash = tx.hashForSignature(0, redeemScript, Transaction.SigHash.ALL, false);
            ECKey.ECDSASignature sig = key.sign(hash);
            TransactionSignature signature = new TransactionSignature(sig, Transaction.SigHash.ALL, false, true);

            scriptSig = ScriptBuilder.updateScriptWithSignature(scriptSig, signature.encodeToBitcoin(), i, 1, 1);
        }
        tx.getInput(0).setScriptSig(scriptSig);

        System.out.println(Utils.HEX.encode(tx.bitcoinSerialize()));

        // TODO: This will fail. org.bitcoincashj.script.Script.executeMultiSig() should be updated
        // tx.getInput(0).verify(output);
    }
*/
    @Test
    public void testP2PKHSign() {
        //************ params & inputs
        //priKey
        String privateKey = "KzzAeiUESLWEXcf5GV1vAfk6rKTacR88BHVQnzuQuLk2h4dLXhhd";
        //token transfer amount
        String amount = "23138600000";
        //odd change output
        Coin changeOutput = Coin.valueOf(0, 99);
        //contract address
        byte[] contractAddress = Utils.HEX.decode("fe59cbc1704e89a698571413a81f0de9d8f00c69"); //INK
        // utxo
        String utxoStr = "c3d8c742bccfc40161bcd4288727d5b77ce653aa483c89ad9090f95c844c89ed";
        int utxoOutput = 0;

        //************ process key & address

        DumpedPrivateKey dumpedPrivateKey = DumpedPrivateKey.fromBase58(PARAMS, privateKey);
        ECKey ecKey = dumpedPrivateKey.getKey();

        Address address = ecKey.toAddress(PARAMS);

        Transaction tx = new Transaction(PARAMS);
        tx.setVersion(1);

        Script scriptPubKey = ScriptBuilder.createOutputScript(address);

        //tx.addInput(new Sha256Hash("c3d8c742bccfc40161bcd4288727d5b77ce653aa483c89ad9090f95c844c89ed"), 0, scriptPubKey, Coin.valueOf(100000000L));
        tx.addInput(new Sha256Hash(utxoStr), utxoOutput, scriptPubKey);
        //************ tx output:token transfer [0]

        byte[] transferAmount = Utils.bigIntegerToBytes(new BigInteger(amount, 10), 32);
        byte[] dataHex = Utils.parseAsHexOrBase58("a9059cbb"
                + "000000000000000000000000"+ Utils.HEX.encode(address.getHash160())
                + Utils.HEX.encode(transferAmount));

        tx.addOutput(2500000, 40, dataHex, contractAddress);

        //************ tx output: value - gasprice * gaslimit - fee [1]
        Address destAddress = Address.fromBase58(PARAMS, "QSVy7wGCN65kkWxxAkMipmTYeHNkUF9UZP");
        tx.addOutput(changeOutput, destAddress);

        //************ sign tx
        Sha256Hash hash = tx.hashForSignatureBtc(0, scriptPubKey, Transaction.SigHash.ALL, false);
        ECKey.ECDSASignature sig = ecKey.sign(hash);

        TransactionSignature signature = new TransactionSignature(sig, Transaction.SigHash.ALL, false, true);
        Script scriptSig = ScriptBuilder.createInputScript(signature, ecKey);
        tx.getInput(0).setScriptSig(scriptSig);

        System.out.println(Utils.HEX.encode(tx.bitcoinSerialize()));
    }
}
