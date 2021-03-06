package lain;

import lain.Types.*;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

import static lain.Types.*;

/**
 * Created by merlin on 16/7/27.
 */
class Core {
    private static LainFunction lainThrow = new LainFunction("throw") {
        @Override
        public LainObj apply(LainList args) throws LainException {
            throw new LainException(args.get(0));
        }
    };

    private static LainFunction plus = new LainFunction("+") {
        @Override
        public LainObj apply(LainList args) throws LainException {
            if (args.size() == 0) {
                return new LainInteger(0);
            } else {
                boolean allInt = true;
                List<LainObj> list = args.getValue();
                for (LainObj arg : list) {
                    if (!LainInteger.class.isAssignableFrom(arg.getClass())) {
                        allInt = false;
                    }
                }
                if (allInt) {
                    int ret = 0;
                    for (LainObj arg : list) {
                        ret = ret + ((LainInteger) arg).intValue();
                    }
                    return new LainInteger(ret);
                } else {
                    double ret = 0d;
                    for (LainObj arg : list) {
                        ret = ret + ((LainNumber) arg).doubleValue();
                    }
                    return new LainDecimal(ret);
                }
            }
        }
    };

    private static LainFunction minus = new LainFunction("-") {
        @Override
        public LainObj apply(LainList args) throws LainException {
            if (args.size() == 0) {
                throw new LainException("wrong number of parameters to '-' function");
            } else if (args.size() == 1) {
                if (LainInteger.class.isAssignableFrom(args.get(0).getClass())) {
                    int first = ((LainInteger) args.get(0)).intValue();
                    return new LainInteger(-first);
                } else {
                    double first = ((LainDecimal) args.get(0)).doubleValue();
                    return new LainDecimal(first);
                }
            } else {
                boolean allInt = true;
                List<LainObj> list = args.getValue();
                for (LainObj arg : list) {
                    if (!LainInteger.class.isAssignableFrom(arg.getClass())) {
                        allInt = false;
                    }
                }
                if (allInt) {
                    int ret = ((LainInteger) args.get(0)).getValue();
                    for (LainObj arg : list.subList(1, list.size())) {
                        ret = ret - ((LainInteger) arg).intValue();
                    }
                    return new LainInteger(ret);
                } else {
                    double ret = ((LainDecimal) args.get(0)).doubleValue();
                    for (LainObj arg : list.subList(1, list.size())) {
                        ret = ret - ((LainNumber) arg).doubleValue();
                    }
                    return new LainDecimal(ret);
                }
            }
        }
    };

    private static LainFunction times = new LainFunction("*") {
        @Override
        public LainObj apply(LainList args) throws LainException {
            if (args.size() == 0) {
                return new LainInteger(1);
            } else {
                boolean allInt = true;
                List<LainObj> list = args.getValue();
                for (LainObj arg : list) {
                    if (!LainInteger.class.isAssignableFrom(arg.getClass())) {
                        allInt = false;
                    }
                }
                if (allInt) {
                    int ret = 1;
                    for (LainObj arg : list) {
                        ret = ret * ((LainInteger) arg).intValue();
                    }
                    return new LainInteger(ret);
                } else {
                    double ret = 1d;
                    for (LainObj arg : list) {
                        ret = ret * ((LainNumber) arg).doubleValue();
                    }
                    return new LainDecimal(ret);
                }
            }
        }
    };

    private static LainFunction divides = new LainFunction("/") {
        @Override
        public LainObj apply(LainList args) throws LainException {
            if (args.size() == 0) {
                throw new LainException("wrong number of parameters to '/' function");
            } else if (args.size() == 1) {
                double first = ((LainNumber) args.get(0)).doubleValue();
                return new LainDecimal(1 / first);
            } else {
                boolean allInt = true;
                List<LainObj> list = args.getValue();
                for (LainObj arg : list) {
                    if (!LainInteger.class.isAssignableFrom(arg.getClass())) {
                        allInt = false;
                    }
                }
                if (allInt) {
                    LainInteger first = ((LainInteger) args.get(0));
                    int ret = first.intValue();
                    List<LainObj> sublist = args.sub(1).getValue();
                    for (LainObj aList : sublist) {
                        ret = ret / ((LainInteger) aList).intValue();
                    }
                    return new LainInteger(ret);
                } else {
                    LainNumber first = ((LainNumber) args.get(0));
                    double ret = first.doubleValue();
                    List<LainObj> sublist = (args.sub(1)).getValue();
                    for (LainObj aList : sublist) {
                        ret = ret / ((LainNumber) aList).doubleValue();
                    }
                    return new LainDecimal(ret);
                }
            }
        }
    };

    private static LainFunction equal = new LainFunction("=") {
        @Override
        public LainObj apply(LainList args) throws LainException {
            int size = args.getValue().size();
            for (int i = 0; i < size; i++) {
                LainList rest = (args.sub(i + 1));
                int restSize = rest.size();
                for (int j = 0; j < restSize; j++) {
                    if (equal(args.get(i), rest.get(j)).equals(False)) {
                        return False;
                    }
                }
            }
            return True;
        }
    };

    private static LainFunction lt = new LainFunction("<") {
        @Override
        public LainObj apply(LainList args) throws LainException {
            int size = args.getValue().size();
            for (int i = 0; i < size; i++) {
                LainList rest = args.sub(i + 1);
                int restSize = rest.size();
                for (int j = 0; j < restSize; j++) {
                    if (LainNumber.lt(
                            (LainNumber) args.get(i),
                            (LainNumber) rest.get(j)).equals(False)) {
                        return False;
                    }
                }
            }
            return True;
        }
    };

    private static LainFunction lte = new LainFunction("<=") {
        @Override
        public LainObj apply(LainList args) throws LainException {
            int size = args.getValue().size();
            for (int i = 0; i < size; i++) {
                LainList rest = args.sub(i + 1);
                int restSize = rest.size();
                for (int j = 0; j < restSize; j++) {
                    if (LainNumber.lte(
                            (LainNumber) args.get(i),
                            (LainNumber) rest.get(j)).equals(False)) {
                        return False;
                    }
                }
            }
            return True;
        }
    };

    private static LainFunction gt = new LainFunction(">") {
        @Override
        public LainObj apply(LainList args) throws LainException {
            int size = args.getValue().size();
            for (int i = 0; i < size; i++) {
                LainList rest = args.sub(i + 1);
                int restSize = rest.size();
                for (int j = 0; j < restSize; j++) {
                    if (LainNumber.gt(
                            (LainNumber) args.get(i),
                            (LainNumber) rest.get(j)).equals(False)) {
                        return False;
                    }
                }
            }
            return True;
        }
    };

    private static LainFunction gte = new LainFunction(">=") {
        @Override
        public LainObj apply(LainList args) throws LainException {
            int size = args.getValue().size();
            for (int i = 0; i < size; i++) {
                LainList rest = args.sub(i + 1);
                int restSize = rest.size();
                for (int j = 0; j < restSize; j++) {
                    if (LainNumber.gte(
                            (LainNumber) args.get(i),
                            (LainNumber) rest.get(j)).equals(False)) {
                        return False;
                    }
                }
            }
            return True;
        }
    };

    private static LainFunction prn = new LainFunction("prn") {
        @Override
        public LainObj apply(LainList args) throws LainException {
            List<String> sj = new ArrayList<>();
            for (LainObj arg : args.getValue()) {
                sj.add(Printer.printStr(arg, true));
            }
            System.out.print(StringUtils.join(sj, " ") + "\n");
            return Nil;
        }
    };

    private static LainFunction prStr = new LainFunction("pr-str") {
        @Override
        public LainObj apply(LainList args) throws LainException {
            List<String> sj = new ArrayList<>();
            for (LainObj arg : args.getValue()) {
                sj.add(Printer.printStr(arg, true));
            }
            return new LainString(StringUtils.join(sj, " "));
        }
    };

    private static LainFunction str = new LainFunction("str") {
        @Override
        public LainObj apply(LainList args) throws LainException {
            List<String> sj = new ArrayList<>();
            for (LainObj arg : args.getValue()) {
                sj.add(Printer.printStr(arg, false));
            }
            return new LainString(StringUtils.join(sj, ""));
        }
    };

    private static LainFunction println = new LainFunction("println") {
        @Override
        public LainObj apply(LainList args) throws LainException {
            List<String> sj = new ArrayList<>();
            for (LainObj arg : args.getValue()) {
                sj.add(Printer.printStr(arg, false));
            }
            System.out.print(StringUtils.join(sj, " ") + "\n");
            return Nil;
        }
    };

    private static LainFunction list = new LainFunction("list") {
        @Override
        public LainObj apply(LainList args) throws LainException {
            return new LainList(args.getValue());
        }
    };

    private static LainFunction isList = new LainFunction("list?") {
        @Override
        public LainObj apply(LainList args) throws LainException {
            for (LainObj arg : args.getValue()) {
                if (arg instanceof LainVector) {
                    return False;
                }
                if (!(arg instanceof LainList)) {
                    return False;
                }
            }
            return True;
        }
    };

    private static LainFunction isEmpty = new LainFunction("empty?") {
        @Override
        public LainObj apply(LainList args) throws LainException {
            for (LainObj arg : args.getValue()) {
                if (!(arg instanceof LainList)) {
                    return False;
                } else if (((LainList) arg).size() != 0) {
                    return False;
                }
            }
            return True;
        }
    };

    private static LainFunction count = new LainFunction("count") {
        @Override
        public LainObj apply(LainList args) throws LainException {
            if (!(args.get(0) instanceof LainList)) {
                return new LainInteger(0);
            } else {
                return new LainInteger(((LainList) args.get(0)).size());
            }
        }
    };

    private static LainFunction readString = new LainFunction("read-string") {
        @Override
        public LainObj apply(LainList args) throws LainException {
            if (args.get(0) instanceof LainString)
                return Reader.readStr(((LainString) args.get(0)).getValue());
            else
                throw new LainException("wrong type of argument to function 'read-string'!");
        }
    };

    private static LainFunction slurp = new LainFunction("slurp") {
        @Override
        public LainObj apply(LainList args) throws LainException {
            try {
                if (!(args.get(0) instanceof LainString)) {
                    throw new LainException("wrong type of arguments to function 'slurp'");
                }
                return new LainString(
                        new String(Files.readAllBytes(
                                Paths.get(((LainString) args.get(0)).getValue()))));

            } catch (IOException | LainException e) {
                throw new LainException("can't find file: " + args.get(0));
            }
        }
    };

    private static LainFunction atom = new LainFunction("atom") {
        @Override
        public LainObj apply(LainList args) throws LainException {
            return new LainAtom(args.get(0));
        }
    };

    private static LainFunction isAtom = new LainFunction("atom?") {
        @Override
        public LainObj apply(LainList args) throws LainException {
            return args.get(0) instanceof LainAtom ? True : False;
        }
    };

    private static LainFunction deRef = new LainFunction("deref") {
        @Override
        public LainObj apply(LainList args) throws LainException {
            return ((LainAtom) args.get(0)).getAtom();
        }
    };

    private static LainFunction resetAtom = new LainFunction("reset!") {
        @Override
        public LainObj apply(LainList args) throws LainException {
            ((LainAtom) args.get(0)).setAtom(args.get(1));
            return args.get(1);
        }
    };

    private static LainFunction swapAtom = new LainFunction("swap!") {
        @Override
        public LainObj apply(LainList args) throws LainException {
            LainFunction func = (LainFunction) args.get(1);
            LainList restArgs = args.sub(2);
            LainAtom atom = (LainAtom) args.get(0);
            LainList newArgs = new LainList();
            newArgs.getValue().addAll(restArgs.getValue());
            newArgs.getValue().add(0, atom.getAtom());
            atom.setAtom(func.apply(newArgs));
            return atom.getAtom();
        }
    };

    private static LainFunction cons = new LainFunction("cons") {
        @Override
        public LainObj apply(LainList args) throws LainException {
            List<LainObj> list = new ArrayList<>();
            list.add(args.get(0));
            list.addAll(((LainList) args.get(1)).getValue());
            return new LainList(list);
        }
    };

    private static LainFunction consVec = new LainFunction("cons-vec") {
        @Override
        public LainObj apply(LainList args) throws LainException {
            List<LainObj> list = new ArrayList<>();
            list.add(args.get(0));
            list.addAll(((LainList) args.get(1)).getValue());
            return new LainVector(list);
        }
    };

    private static LainFunction concat = new LainFunction("concat") {
        @Override
        public LainObj apply(LainList args) throws LainException {
            List<LainObj> list = new ArrayList<>();
            for (LainObj arg : args.getValue()) {
                list.addAll(((LainList) arg).getValue());
            }
            return new LainList(list);
        }
    };

    private static LainFunction concatVec = new LainFunction("concat-vec") {
        @Override
        public LainObj apply(LainList args) throws LainException {
            List<LainObj> list = new ArrayList<>();
            for (LainObj arg : args.getValue()) {
                list.addAll(((LainList) arg).getValue());
            }
            return new LainVector(list);
        }
    };

    private static LainFunction nth = new LainFunction("nth") {
        @Override
        public LainObj apply(LainList args) throws LainException {
            int index = ((LainInteger) args.get(1)).getValue();
            if (index < ((LainList) args.get(0)).size()) {
                return ((LainList) args.get(0)).get(index);
            } else {
                throw new LainException("nth: index out of bound!");
            }
        }
    };

    private static LainFunction first = new LainFunction("first") {
        @Override
        public LainObj apply(LainList args) throws LainException {
            LainObj first = args.get(0);
            if (first == Nil) {
                return Nil;
            }
            LainList list = (LainList) first;
            return list.size() > 0 ? list.get(0) : Nil;
        }
    };

    private static LainFunction rest = new LainFunction("rest") {
        @Override
        public LainObj apply(LainList args) throws LainException {
            LainObj first = args.get(0);
            if (first == Nil) {
                return new LainList();
            }
            LainList list = (LainList) first;
            return list.rest();
        }
    };

    private static LainFunction apply = new LainFunction("apply") {
        @Override
        public LainObj apply(LainList args) throws LainException {
            LainFunction func = (LainFunction) args.get(0);
            LainList realArgs = args.sub(1, args.size() - 1);
            realArgs.getValue().addAll(((LainList) args.get(args.size() - 1)).getValue());
            return func.apply(realArgs);
        }
    };

    private static LainFunction map = new LainFunction("map") {
        @Override
        public LainObj apply(LainList args) throws LainException {
            LainFunction func = (LainFunction) args.get(0);
            LainList list = (LainList) args.get(1);
            LainList ret = new LainList();
            for (LainObj item : list.getValue()) {
                ret.getValue().add(func.apply(new LainList(item)));
            }
            return ret;
        }
    };

    private static LainFunction isNil = new LainFunction("nil?") {
        @Override
        public LainObj apply(LainList args) throws LainException {
            return args.get(0) == Nil ? True : False;
        }
    };

    private static LainFunction isTrue = new LainFunction("true?") {
        @Override
        public LainObj apply(LainList args) throws LainException {
            return args.get(0) == True ? True : False;
        }
    };

    private static LainFunction isFalse = new LainFunction("false?") {
        @Override
        public LainObj apply(LainList args) throws LainException {
            return args.get(0) == False ? True : False;
        }
    };

    private static LainFunction isSymbol = new LainFunction("symbol?") {
        @Override
        public LainObj apply(LainList args) throws LainException {
            return args.get(0) instanceof LainSymbol ? True : False;
        }
    };

    private static LainFunction symbol = new LainFunction("symbol") {
        @Override
        public LainObj apply(LainList args) throws LainException {
            if (args.get(0) instanceof LainString) {
                return new LainSymbol(((LainString) args.get(0)).getValue());
            } else {
                throw new LainException("wrong type of arguments to function 'symbol'!");
            }
        }
    };

    private static LainFunction keyword = new LainFunction("keyword") {
        @Override
        public LainObj apply(LainList args) throws LainException {
            if (args.get(0) instanceof LainKeyword) {
                return args.get(0);
            } else if (args.get(0) instanceof LainString) {
                return new LainString("\u029e" + ((LainString) args.get(0)).getValue());
            } else {
                throw new LainException("wrong type of arguments to function 'keyword'!");
            }
        }
    };

    private static LainFunction isKeyword = new LainFunction("keyword?") {
        @Override
        public LainObj apply(LainList args) throws LainException {
            return args.get(0) instanceof LainKeyword ? True : False;
        }
    };

    private static LainFunction vector = new LainFunction("vector") {
        @Override
        public LainObj apply(LainList args) throws LainException {
            return new LainVector(args.getValue());
        }
    };

    private static LainFunction isVector = new LainFunction("vector?") {
        @Override
        public LainObj apply(LainList args) throws LainException {
            return args.get(0) instanceof LainVector ? True : False;
        }
    };

    private static LainFunction hashMap = new LainFunction("hash-map") {
        @Override
        public LainObj apply(LainList args) throws LainException {
            return new LainHashMap(args);
        }
    };

    private static LainFunction isHashMap = new LainFunction("map?") {
        @Override
        public LainObj apply(LainList args) throws LainException {
            return args.get(0) instanceof LainHashMap ? True : False;
        }
    };

    private static LainFunction assoc = new LainFunction("assoc") {
        @Override
        public LainObj apply(LainList args) throws LainException {
            LainHashMap from = (LainHashMap) args.get(0);
            HashMap to = new HashMap();
            to.putAll(from.getValue());
            LainHashMap ret = new LainHashMap(to);
            ret.accumulate(args.sub(1));
            return ret;
        }
    };

    private static LainFunction dissoc = new LainFunction("dissoc") {
        @Override
        public LainObj apply(LainList args) throws LainException {
            LainHashMap from = (LainHashMap) args.get(0);
            HashMap to = new HashMap();
            to.putAll(from.getValue());
            for (LainObj key : args.sub(1).getValue()) {
                if (to.get(key) != null) {
                    to.remove(key);
                }
            }
            LainHashMap ret = new LainHashMap(to);
            return ret;
        }
    };

    private static LainFunction hashMapGet = new LainFunction("get") {
        @Override
        public LainObj apply(LainList args) throws LainException {
            if (!(args.get(0) instanceof LainHashMap)) {
                return Nil;
            } else {
                return ((LainHashMap) args.get(0)).get((LainAtom) args.get(1));
            }
        }
    };

    private static LainFunction contains = new LainFunction("contains?") {
        @Override
        public LainObj apply(LainList args) throws LainException {
            return ((LainHashMap) args.get(0)).contains((LainAtom) args.get(1));
        }
    };

    private static LainFunction keys = new LainFunction("keys") {
        @Override
        public LainObj apply(LainList args) throws LainException {
            return ((LainHashMap) args.get(0)).keys();
        }
    };

    private static LainFunction values = new LainFunction("vals") {
        @Override
        public LainObj apply(LainList args) throws LainException {
            return ((LainHashMap) args.get(0)).values();
        }
    };

    private static LainFunction isSequential = new LainFunction("sequential?") {
        @Override
        public LainObj apply(LainList args) throws LainException {
            return args.get(0) instanceof LainList ? True : False;
        }
    };

    private static LainFunction readLine = new LainFunction("readline") {
        @Override
        public LainObj apply(LainList args) throws LainException {
            String prompt = ((LainString) args.get(0)).getValue();
            try {
                return new LainString(ReadLine.jnaReadLine(prompt));
            } catch (ReadLine.EOFException e) {
                return Nil;
            } catch (IOException e) {
                throw new LainException(new LainString(e.getMessage()));
            }
        }
    };

    private static LainFunction time_ms = new LainFunction("time-ms") {
        @Override
        public LainObj apply(LainList args) throws LainException {
            return new LainDecimal((double) System.currentTimeMillis());
        }
    };

    private static LainFunction isString = new LainFunction("string?") {
        @Override
        public LainObj apply(LainList args) throws LainException {
            return args.get(0) instanceof LainString ? True : False;
        }
    };

    static LainFunction conj = new LainFunction("conj") {
        public LainObj apply(LainList a) throws LainException {
            LainList src_seq = (LainList) a.get(0), new_seq;
            if (a.get(0) instanceof LainVector) {
                new_seq = new LainVector();
                new_seq.getValue().addAll(src_seq.getValue());
                for (Integer i = 1; i < a.size(); i++) {
                    new_seq.getValue().add(a.get(i));
                }
            } else {
                new_seq = new LainList();
                new_seq.getValue().addAll(src_seq.getValue());
                for (Integer i = 1; i < a.size(); i++) {
                    new_seq.getValue().add(0, a.get(i));
                }
            }
            return new_seq;
        }
    };

    static LainFunction seq = new LainFunction("seq") {
        public LainObj apply(LainList a) throws LainException {
            LainObj mv = (LainObj) a.get(0);
            if (mv instanceof LainVector) {
                if (((LainVector) mv).size() == 0) {
                    return Nil;
                }
                return new LainList(((LainVector) mv).getValue());
            } else if (mv instanceof LainList) {
                if (((LainList) mv).size() == 0) {
                    return Nil;
                }
                return mv;
            } else if (mv instanceof LainString) {
                String s = ((LainString) mv).getValue();
                if (s.length() == 0) {
                    return Nil;
                }
                List<LainObj> lst = new ArrayList<LainObj>();
                for (String c : s.split("(?!^)")) {
                    lst.add(new LainString(c));
                }
                return new LainList(lst);
            } else if (mv == Nil) {
                return Nil;
            } else {
                throw new LainException("seq: called on non-sequence");
            }
        }
    };

    private static Map<String, LainObj> core = new HashMap<>();

    static {
        put(plus);
        put(minus);
        put(times);
        put(divides);
        put(True);
        put(False);
        put(Nil);
        put(equal);
        put(lt);
        put(lte);
        put(gt);
        put(gte);
        put(prn);
        put(prStr);
        put(str);
        put(println);
        put(list);
        put(isList);
        put(isEmpty);
        put(count);
        put(readString);
        put(slurp);
        put(atom);
        put(isAtom);
        put(deRef);
        put(resetAtom);
        put(swapAtom);
        put(cons);
        put(concat);
        put(concatVec);
        put(consVec);
        put(nth);
        put(first);
        put(rest);
        put(lainThrow);
        put(apply);
        put(map);
        put(isNil);
        put(isTrue);
        put(isFalse);
        put(isSymbol);
        put(symbol);
        put(keyword);
        put(isKeyword);
        put(vector);
        put(isVector);
        put(hashMap);
        put(isHashMap);
        put(assoc);
        put(dissoc);
        put(hashMapGet);
        put(contains);
        put(keys);
        put(values);
        put(isSequential);
        put(time_ms);
        put(isString);
        put(conj);
        put(seq);
    }

    private static void put(LainObj func) {
        core.put(func.toString(), func);
    }

    static Map<String, LainObj> ns = Collections.unmodifiableMap(core);
}
