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
    //@Test
    public void testP2PKHSign() {
        //************ params & inputs
        //priKey
        String privateKey = "KzzAeiUESLWEXcf5GV1vAfk6rKTacR88BHVQnzuQuLk2h4dLXhhd";
        //token transfer amount
        String amount = "13128600000"; //23.1286
        //odd change output
        Coin changeOutput = Coin.valueOf(58769000); //0.69769
        //contract address
        byte[] contractAddress = Utils.HEX.decode("fe59cbc1704e89a698571413a81f0de9d8f00c69"); //INK
        // utxo
        String utxoStr = "01df83f210612016ca522381b0a3f0c5d7a1fcd0a72c7dcb4f976b5712e7351e";
        // toAddress
        String toAddress = "QV8c54DVkT4ZqLPb5bYWrixXaJPZzZDjrH";
        int utxoOutput = 1;

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

        Address destAddress = Address.fromBase58(PARAMS, toAddress);

        byte[] transferAmount = Utils.bigIntegerToBytes(new BigInteger(amount, 10), 32);
        byte[] dataHex = Utils.parseAsHexOrBase58("a9059cbb"
                + "000000000000000000000000"+ Utils.HEX.encode(destAddress.getHash160())
                + Utils.HEX.encode(transferAmount));

        tx.addOutput(250000, 40, dataHex, contractAddress);

        //************ tx output: value - gasprice * gaslimit - fee [1]
        tx.addOutput(changeOutput, address);

        //************ sign tx
        Sha256Hash hash = tx.hashForSignatureBtc(0, scriptPubKey, Transaction.SigHash.ALL, false);
        ECKey.ECDSASignature sig = ecKey.sign(hash);

        TransactionSignature signature = new TransactionSignature(sig, Transaction.SigHash.ALL, false);
        Script scriptSig = ScriptBuilder.createInputScript(signature, ecKey);
        tx.getInput(0).setScriptSig(scriptSig);

        System.out.println(Utils.HEX.encode(tx.bitcoinSerialize()));
    }

    class UTXOItem {
        UTXOItem(String priKey, String utxoStr, int index) {
            this.privateKey = priKey;
            this.utxoStr = utxoStr;
            this.utxoIdx = index;
            DumpedPrivateKey dumpedPrivateKey = DumpedPrivateKey.fromBase58(PARAMS, this.privateKey);
            this.ecKey = dumpedPrivateKey.getKey();
            this.address = ecKey.toAddress(PARAMS);
        }
        Address address;
        String privateKey;
        String utxoStr;
        int utxoIdx;
        ECKey ecKey;
    }
    @Test
    public void testMVIO() {
        UTXOItem item1 = new UTXOItem("KzzAeiUESLWEXcf5GV1vAfk6rKTacR88BHVQnzuQuLk2h4dLXhhd", "49c3257d1237fc95113c7056b4c95ffbb8242ea32f0cc8618e269b22c63d138a", 1);
  //      UTXOItem item2 = new UTXOItem("KzzAeiUESLWEXcf5GV1vAfk6rKTacR88BHVQnzuQuLk2h4dLXhhd", "61e9a62bf8f11776a6831e47a69a4d80676f8afa4e32b7ee577315f8da9bbe97", 12);
        UTXOItem item3 = new UTXOItem("L2whmpdrbBVTmyTHmnVxYfGCH62Y3nYTdZk9fYUs5kZEQP5xZoUJ", "6c7f1adec913aba1d06dbadd1e23d90f69dffbcfe8972f34ef8192996df50ff2", 1);
        List<UTXOItem> UTXOs = new ArrayList<>();
        List<Script> scripts = new ArrayList<>();
        UTXOs.add(item1);
        UTXOs.add(item3);
        //UTXOs.add(item3);
        //************ params & inputs
        // toAddress
        String toAddress = "QV8c54DVkT4ZqLPb5bYWrixXaJPZzZDjrH";
        // changeAddress
        String changeAddress = "QSVy7wGCN65kkWxxAkMipmTYeHNkUF9UZP";
        //token amount
        String amount = "10000000000"; //10.00000
        //odd change output
        Coin changeAmount = Coin.valueOf(133883000); //1.33883
        //contract address
        byte[] contractAddress = Utils.HEX.decode("fe59cbc1704e89a698571413a81f0de9d8f00c69"); //INK


        Transaction tx = new Transaction(PARAMS);
        tx.setVersion(1);

        //************ tx input
        for(int i = 0; i < UTXOs.size(); i++) {
            UTXOItem item = UTXOs.get(i);
            Script scriptPubKey = ScriptBuilder.createOutputScript(item.address);
            tx.addInput(new Sha256Hash(item.utxoStr),item.utxoIdx, scriptPubKey);
            scripts.add(scriptPubKey);
        }

        //************ tx output:token transfer [0]

        Address destAddress = Address.fromBase58(PARAMS, toAddress);

        byte[] transferAmount = Utils.bigIntegerToBytes(new BigInteger(amount, 10), 32);
        byte[] dataHex = Utils.parseAsHexOrBase58("a9059cbb"
                + "000000000000000000000000"+ Utils.HEX.encode(destAddress.getHash160())
                + Utils.HEX.encode(transferAmount));
        tx.addOutput(250000, 40, dataHex, contractAddress);
        //************ tx output: value - gasprice * gaslimit - fee [1]
        tx.addOutput(changeAmount, Address.fromBase58(PARAMS,changeAddress));

        //************ sign tx
        int numInputs = tx.getInputs().size();
        for(int i = 0; i < numInputs; i++) {
            UTXOItem item = UTXOs.get(i);
            Script scriptPubKey = scripts.get(i);
            Sha256Hash hash = tx.hashForSignatureBtc(i, scriptPubKey, Transaction.SigHash.ALL, false);
            ECKey.ECDSASignature sig = item.ecKey.sign(hash);
            TransactionSignature signature = new TransactionSignature(sig, Transaction.SigHash.ALL, false);
            Script scriptSig = ScriptBuilder.createInputScript(signature, item.ecKey);
            tx.getInput(i).setScriptSig(scriptSig);
        }
        System.out.println(Utils.HEX.encode(tx.bitcoinSerialize()));
    }
}
