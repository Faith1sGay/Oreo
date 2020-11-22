package io.github.faith1sgay.oreo;

import com.mewna.catnip.Catnip;
import com.mewna.catnip.entity.message.Message;
import com.mewna.catnip.entity.message.ReactionUpdate;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import java.util.List;

public class NoteResponse {
    private int offset;
    private final Catnip catnip;
    private final String messageId;
    private final String channelId;
    private final List<Pair<String, String>> notes;

    public NoteResponse(int offset, List<Pair<String, String>> notes, @Nonnull Message message) {
        this.notes = notes;
        this.offset = offset;
        this.catnip = message.catnip();
        this.messageId = message.id();
        this.channelId = message.channelId();
    }

    public void handleReaction(@Nonnull ReactionUpdate reactionUpdate) {
        if (reactionUpdate.emoji().is("\u23ee\ufe0f")) {
            this.offset = 0;
        }

        Pair<String, Integer> result = NotesCommand.buildMessage(this.notes, this.offset);

        if (result.getRight() == this.offset) return;

        this.offset = result.getRight();

        // todo: handle errors (as the message might have been deleted... not high priority)
        this.catnip.rest().channel().editMessage(this.channelId, this.messageId, result.getLeft());
    }
}
