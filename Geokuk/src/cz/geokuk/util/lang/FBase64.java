package cz.geokuk.util.lang;


/**
 * Konverze binárních dat do a z soustavy Bas64. Koverze jsou velmi silně optimalizovány na rychlost.
 *
 * @author	Martin Veverka>
 * @version $Revision: 10 $
 * @see     "TW0139Util.vjp"
 * @see		"TW0162: Java - Elementární datové typy.doc"
 * @see     "$Header: /Zakazky/TWare/Distribuce/TW0139/Util/cz/tconsult/tw/data/TwBinary.java 10    27.09.00 19:20 Polakm $"
 * @since	NQT 2.04
 */

//Třída obsahující funkce pro konverzi Base64. Silně optimalizováno.
//Too je optimalizvaná verze BASE64. Lépe to snad optimalizovat nepůjde,
//tak na to nehrab! Ani pokud se ti to nelíbí.
public final class FBase64 {

        private static final char[] base64Table = {
                'A','B','C','D','E','F','G','H','I','J','K','L','M','N','O','P',   //16
                'Q','R','S','T','U','V','W','X','Y','Z','a','b','c','d','e','f',   //32
                'g','h','i','j','k','l','m','n','o','p','q','r','s','t','u','v',   //48
                'w','x','y','z','0','1','2','3','4','5','6','7','8','9','+','/'    //64
                };

        private static final char[] inverseBase64Table = {
                255,255,255,255,255,255,255,255,255,255,255,255,255,255,255,255,   //16
                255,255,255,255,255,255,255,255,255,255,255,255,255,255,255,255,   //32
                255,255,255,255,255,255,255,255,255,255,255, 62,255,255,255, 63,   //48
                 52, 53, 54, 55, 56, 57, 58, 59, 60, 61,255,255,255,255,255,255,   //64
                255,  0,  1,  2,  3,  4,  5,  6,  7,  8,  9, 10, 11, 12, 13, 14,   //80
                 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25,255,255,255,255,255,   //96
                255, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40,   //112
                 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51};					   //123

        /**Zakódování zadaného pole bytů do soustavy base64
        */
        public static String encode (byte[] b) {

                if (b == null) return null;

                StringBuffer s = new StringBuffer(b.length * 4 / 3 + 10);

                int delka = b.length / 3 * 3;
                //Nejdříve vyřešíme plné triplety
                for (int i=0; i<delka; i+=3)
                {
                        int bi0 = b[i];
                        int bi1 = b[i+1];
                        int bi2 = b[i+2];
                        int b1 = ((bi0 >> 2) & 0x3F);
                        int b2 = ((bi0 & 0x03) << 4);
                        b2 += ((bi1 >> 4) & 0x0F);
                        int b3 = ((bi1 & 0x0F) << 2);
                        b3 += ((bi2 >> 6) & 0x03);
                        int b4 = (bi2 & 0x3F);
                        s.append(base64Table[b1]);
                        s.append(base64Table[b2]);
                        s.append(base64Table[b3]);
                        s.append(base64Table[b4]);
                }

                //Nakonec vyřešíme poslední neúplný triplet
                if (b.length > delka)
                { // pokud máme nějaké neúplné triplety
                        int b1 = ((b[delka] >> 2) & 0x3F);
                        int b2 = ((b[delka] & 0x03) << 4);

                        if (b.length - delka == 1)
                        {
                                s.append(base64Table[b1]);
                                s.append(base64Table[b2]);
                                s.append('=');
                                s.append('=');
                        } else
                        {
                                b2 += ((b[delka+1] >> 4) & 0x0F);
                                int b3 = ((b[delka+1] & 0x0F) << 2);
                                s.append(base64Table[b1]);
                                s.append(base64Table[b2]);
                                s.append(base64Table[b3]);
                                s.append('=');
                        }
                }
                return s.toString();
        }

        /**Rozkódování zadaného pole bytů do soustavy Base64
        */
        public static byte[] decode (String s)
        {
                boolean jsoudva = false, jsoutri = false;
                int srcLenFull = s.length(); //bude nakonec obsahovat délku s plnými čtyřplety
                int destLen = s.length() / 4 * 3; //předpokládámě, že délka řetězce je násobkem čtyř
                if (s.endsWith("="))
                {
                        srcLenFull-=4; //čtyřplety nejsou plné
                        destLen --; //Pokud je na konci rovnítko, je o byte méně
                        if (s.endsWith("=="))
                        {
                                destLen --; //A pokud jsou tam dvě tak ještě méně
                                jsoudva = true;
                        } else
                                jsoutri = true;
                }
                // tak alokujeme výstupní buffer o již známé délce
                byte b[] = new byte[destLen];
                //Délka v celých tripletech
                //Nejdříve vyřešíme plné čtyřplety
                int j=0; //pozice ve výsledném políčku
                int i=0;
                for (i=0, j=0; i<srcLenFull; i+=4, j+=3)
                {
                        char c0 = inverseBase64Table[s.charAt(i)];
                        char c1 = inverseBase64Table[s.charAt(i+1)];
                        char c2 = inverseBase64Table[s.charAt(i+2)];
                        char c3 = inverseBase64Table[s.charAt(i+3)];

                        b[j] = (byte)((c0 << 2) + ((c1 & 0x30) >>> 4));
                        b[j+1] = (byte)(((c1 & 0x0F) << 4) + ((c2 & 0x3C) >>> 2));
                        b[j+2] = (byte)(((c2 & 0x03) << 6) + c3);
                }
                //A teď musíme vyřešit zbytek, mohou to být dva nebo tři znaky, ne však jeden
                if (jsoudva || jsoutri) // jen v tom případě zbývá čtyřplet
                {
                        char c0 = inverseBase64Table[s.charAt(srcLenFull)];
                        char c1 = inverseBase64Table[s.charAt(srcLenFull+1)];
                        b[j] = (byte)((c0 << 2) + ((c1 & 0x30) >>> 4));
                        if (jsoutri)
                        {
                                char c2 = inverseBase64Table[s.charAt(srcLenFull+2)];
                                b[j+1] = (byte)(((c1 & 0x0F) << 4) + ((c2 & 0x3C) >>> 2));
                        }
                }
                return b;
        }

}
