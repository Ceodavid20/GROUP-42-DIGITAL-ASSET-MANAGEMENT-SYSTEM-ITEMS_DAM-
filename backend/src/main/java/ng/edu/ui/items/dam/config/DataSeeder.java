package ng.edu.ui.items.dam.config;

import ng.edu.ui.items.dam.entity.*;
import ng.edu.ui.items.dam.enums.AssetStatus;
import ng.edu.ui.items.dam.enums.ERole;
import ng.edu.ui.items.dam.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.Optional;
import java.util.Set;

/**
 * Appendix C: initial reference dataset for acceptance walkthrough (TC-01.TC-12).
 * Roles follow Table 3.2 (ADMIN / ASSET_OFFICER / UNIT_CUSTODIAN /
 * MAINTENANCE_TECHNICIAN / AUDITOR).
 */
@Configuration
public class DataSeeder {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    @Bean
    @Order(1)
    @Profile("!prod")
    public CommandLineRunner seed(RoleRepository roles, UserRepository users,
                                  CategoryRepository categories, LocationRepository locations,
                                  AssetRepository assets, PasswordEncoder encoder) {
        return args -> {
            log.info("Reference data seeding started");

            long usersBefore = users.count();
            long categoriesBefore = categories.count();
            long locationsBefore = locations.count();
            long assetsBefore = assets.count();

            for (ERole r : ERole.values()) {
                Optional<Role> existing = roles.findByName(r);
                if (existing.isPresent()) {
                    log.debug("Skipping role {} (already present)", r);
                } else {
                    roles.save(Role.builder().name(r).description(r.name()).build());
                    log.info("Seeded role {}", r);
                }
            }
            Role admin = roles.findByName(ERole.ROLE_ADMIN).orElseThrow();
            Role officer = roles.findByName(ERole.ROLE_ASSET_OFFICER).orElseThrow();
            Role custodian = roles.findByName(ERole.ROLE_UNIT_CUSTODIAN).orElseThrow();
            Role tech = roles.findByName(ERole.ROLE_MAINTENANCE_TECHNICIAN).orElseThrow();
            Role auditor = roles.findByName(ERole.ROLE_AUDITOR).orElseThrow();

            ensureUser(users, encoder, "admin", "System Administrator",
                    "UI/ITeMS/ADMIN/001", "admin@items.ui.edu.ng", "admin123",
                    "ITeMS Directorate", admin);
            ensureUser(users, encoder, "officer", "Asset Officer",
                    "UI/ITeMS/OFF/002", "officer@items.ui.edu.ng", "officer123",
                    "Network & Hardware Unit", officer);
            User custodianU = ensureUser(users, encoder, "custodian", "Unit Custodian",
                    "UI/ITeMS/CUS/003", "custodian@items.ui.edu.ng", "custodian123",
                    "MIS Unit", custodian);
            ensureUser(users, encoder, "technician", "Maintenance Technician",
                    "UI/ITeMS/TEC/004", "technician@items.ui.edu.ng", "technician123",
                    "Hardware Lab", tech);
            ensureUser(users, encoder, "auditor", "Auditor",
                    "UI/ITeMS/AUD/005", "auditor@items.ui.edu.ng", "auditor123",
                    "Audit Unit", auditor);

            Category comp = ensureCategory(categories, "Computing Devices", "COMP",
                    "Desktops, laptops, peripherals");
            Category net = ensureCategory(categories, "Network Hardware", "NET",
                    "Switches, access points, routers");
            Category power = ensureCategory(categories, "Power Equipment", "PWR",
                    "UPS, inverters, stabilizers");
            Category display = ensureCategory(categories, "Display & Projection", "DISP",
                    "Projectors, monitors, screens");

            Location hw = ensureLocation(locations, "Network & Hardware Unit",
                    "ITeMS Complex", "Hardware Lab 102", "Main Campus");
            Location mis = ensureLocation(locations, "MIS Unit",
                    "ITeMS Complex", "Server Room B", "Main Campus");
            Location media = ensureLocation(locations, "Media Centre",
                    "Media Block", "Studio 1", "Main Campus");

            mkAsset(assets, "UI-ITEMS-001", "Desktop PC", comp, hw, AssetStatus.REGISTERED,
                    "BRAND-A", "Model 100", null, null, null);
            mkAsset(assets, "UI-ITEMS-002", "Laptop", comp, mis, AssetStatus.AVAILABLE,
                    "BRAND-B", "Notebook 14", "SN-0002", "AA:BB:CC:00:00:02", null);
            mkAsset(assets, "UI-ITEMS-003", "Projector", display, media, AssetStatus.ASSIGNED,
                    "BRAND-C", "Proj X1", "SN-0003", null, custodianU);
            mkAsset(assets, "UI-ITEMS-004", "Switch 24-port", net, hw, AssetStatus.UNDER_MAINTENANCE,
                    "BRAND-D", "Switch 24", "SN-0004", "AA:BB:CC:00:00:04", null);
            mkAsset(assets, "UI-ITEMS-005", "UPS 3kVA", power, mis, AssetStatus.RETIRED,
                    "BRAND-E", "UPS 3000", "SN-0005", null, null);
            mkAsset(assets, "UI-ITEMS-006", "Access Point", net, hw, AssetStatus.DISPOSED,
                    "BRAND-F", "AP AC", "SN-0006", "AA:BB:CC:00:00:06", null);

            long addedUsers = users.count() - usersBefore;
            long addedCategories = categories.count() - categoriesBefore;
            long addedLocations = locations.count() - locationsBefore;
            long addedAssets = assets.count() - assetsBefore;

            if (addedUsers + addedCategories + addedLocations + addedAssets == 0) {
                log.info("Reference data seeding skipped: all seed records already present");
            } else {
                log.info("Reference data seeding completed: {} user(s), {} category(ies), "
                                + "{} location(s), {} asset(s) created",
                        addedUsers, addedCategories, addedLocations, addedAssets);
            }
        };
    }

    private User ensureUser(UserRepository users, PasswordEncoder encoder, String username, String name,
                            String workIdentifier, String email, String rawPassword, String unitName,
                            Role role) {
        Optional<User> existing = users.findByUsername(username);
        if (existing.isPresent()) {
            log.debug("Skipping user '{}' (already present)", username);
            return existing.get();
        }
        log.info("Seeding user '{}'", username);
        return users.save(User.builder().name(name).username(username)
                .workIdentifier(workIdentifier).email(email)
                .password(encoder.encode(rawPassword)).unitName(unitName)
                .enabled(true).roles(Set.of(role)).build());
    }

    private Category ensureCategory(CategoryRepository categories, String name, String code,
                                    String description) {
        Optional<Category> existing = categories.findByName(name);
        if (existing.isPresent()) {
            log.debug("Skipping category '{}' (already present)", name);
            return existing.get();
        }
        log.info("Seeding category '{}'", name);
        return categories.save(Category.builder().name(name).code(code)
                .description(description).build());
    }

    private Location ensureLocation(LocationRepository locations, String unitName, String building,
                                    String room, String campusZone) {
        Optional<Location> existing = locations.findByUnitName(unitName).stream()
                .filter(l -> room.equals(l.getRoom()))
                .findFirst();
        if (existing.isPresent()) {
            log.debug("Skipping location '{}/{}' (already present)", unitName, room);
            return existing.get();
        }
        log.info("Seeding location '{}/{}'", unitName, room);
        return locations.save(Location.builder().unitName(unitName).building(building)
                .room(room).campusZone(campusZone).build());
    }

    private Asset mkAsset(AssetRepository assets, String tag, String name, Category cat, Location loc,
                          AssetStatus status, String brand, String model, String serial, String mac,
                          User custodian) {
        Optional<Asset> existing = assets.findByAssetTag(tag);
        if (existing.isPresent()) {
            log.debug("Skipping asset {} (already present)", tag);
            return existing.get();
        }
        log.info("Seeding asset {}", tag);
        return assets.save(Asset.builder()
                .assetTag(tag).name(name).category(cat).location(loc).status(status)
                .brand(brand).model(model).serialNumber(serial).macAddress(mac)
                .currentCustodian(custodian)
                .purchaseDate(LocalDate.of(2023, 9, 1))
                .warrantyExpiry(status == AssetStatus.REGISTERED ? null : LocalDate.of(2026, 9, 1))
                .specifications("specifications for " + tag)
                .notes("RECORD")
                .qrCodeData(tag)
                .build());
    }
}
