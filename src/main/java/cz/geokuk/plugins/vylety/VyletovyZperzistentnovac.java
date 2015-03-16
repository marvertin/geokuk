package cz.geokuk.plugins.vylety;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

import cz.geokuk.core.program.FConst;
import cz.geokuk.plugins.kesoid.KesBag;
import cz.geokuk.plugins.kesoid.Kesoid;
import cz.geokuk.plugins.kesoid.mvc.KesoidModel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class VyletovyZperzistentnovac {

    private static final Logger log = LogManager.getLogger(VyletovyZperzistentnovac.class.getSimpleName());

    private KesoidModel kesoidModel;

    private VyletPul loadGgt(File file) throws IOException {
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            return loadGgt(br);
        } catch (FileNotFoundException e) {
            return new VyletPul(new HashSet<String>());
        }
    }

    public Vylet immediatlyNactiVylet(KesBag vsechny) {
        try {
            Vylet novyvylet = new Vylet();
            aktualizujVylet(novyvylet, loadGgt(kesoidModel.getUmisteniSouboru().getAnoGgtFile().getEffectiveFile()),
                    EVylet.ANO, vsechny);
            aktualizujVylet(novyvylet, loadGgt(kesoidModel.getUmisteniSouboru().getNeGgtFile().getEffectiveFile()),
                    EVylet.NE, vsechny);
            return novyvylet;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private VyletPul loadGgt(BufferedReader reader) throws IOException {
        String line;
        Set<String> set = new HashSet<>();
        while ((line = reader.readLine()) != null) {
            line = line.trim();
            if (line.isEmpty()) {
                continue;
            }
            set.add(line);
        }
        return new VyletPul(set);
    }

    private void aktualizujVylet(Vylet novyvylet, VyletPul vyletPul, EVylet evyl, KesBag vsechny) {
        if (vsechny != null) {
            for (Kesoid kes : vsechny.getKesoidy()) {
                if (vyletPul.kesides.contains(kes.getIdentifier())) {
                    novyvylet.add(evyl, kes);
                }
            }
        }
    }

    public void immediatlyZapisVylet(Vylet vylet) {
        zapis(vylet, kesoidModel.getUmisteniSouboru().getAnoGgtFile().getEffectiveFile(), EVylet.ANO);
        zapis(vylet, kesoidModel.getUmisteniSouboru().getNeGgtFile().getEffectiveFile(), EVylet.NE);
    }

    private void zapis(Vylet vylet, File file, EVylet evyl) {
        Set<Kesoid> caches = vylet.get(evyl);
        Set<String> tripGeocodes = new HashSet<>(caches.size());
        for (Kesoid cache : caches) {
            tripGeocodes.add(cache.getIdentifier());
        }
        List<String> toWrite = new ArrayList<>();

        boolean rewrite = false;

        if (file.exists() && file.canRead()) {
            try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = br.readLine()) != null) {
                    if (tripGeocodes.contains(line)) {
                        toWrite.add(line);
                        tripGeocodes.remove(line);
                    } else {
                        rewrite = true;
                    }
                }
            } catch (IOException e) {
                rewrite = true;
                log.error("Error while reading a trip file!", e);
            }
        }

        if (rewrite) {
            toWrite.addAll(tripGeocodes);
            flushToFile(toWrite, file, false);
            log.info("Rewritten all for trip {}.", evyl);
        } else if (!tripGeocodes.isEmpty()) {
            flushToFile(tripGeocodes, file, true);
            log.info("Appended {} caches(s) for trip {}", tripGeocodes.size(), evyl);
        } else {
            log.info("No changes detected, keeping the original file for trip {}.", evyl);
        }
    }

    private void flushToFile(Collection<String> lines, File file, boolean append) {
        try (BufferedWriter wrt = new BufferedWriter(new FileWriter(file, append))) {
            // TODO : portability
            for (String s : lines) {
                wrt.write(String.format("%s%s", s, FConst.NL));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void inject(KesoidModel kesoidModel) {
        this.kesoidModel = kesoidModel;
    }
}
