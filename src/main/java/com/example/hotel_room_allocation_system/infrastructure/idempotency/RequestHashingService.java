package com.example.hotel_room_allocation_system.infrastructure.idempotency;

import com.example.hotel_room_allocation_system.api.dto.OccupancyRequest;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;

@Component
public class RequestHashingService {

    public String hash(OccupancyRequest request, boolean explain){
        MessageDigest digest = sha256();

        digest.update(intBytes(request.premiumRooms()));
        digest.update(intBytes(request.economyRooms()));
        digest.update((byte) (explain ? 1:0));

        for(BigDecimal g : request.potentialGuests()){
            if(g == null){
                digest.update((byte) 0);
                continue;
            }

            String s = g.stripTrailingZeros().toPlainString();
            digest.update(s.getBytes(StandardCharsets.UTF_8));
            digest.update((byte) 0);
        }

        return HexFormat.of().formatHex(digest.digest());
    }

    private static MessageDigest sha256(){
        try {
            return MessageDigest.getInstance("SHA-256");
        } catch (Exception e) {
            throw new IllegalArgumentException("SHA 256 not available", e);
        }
    }

    private static byte[] intBytes(int v) { return ByteBuffer.allocate(4).putInt(v).array();}
}
