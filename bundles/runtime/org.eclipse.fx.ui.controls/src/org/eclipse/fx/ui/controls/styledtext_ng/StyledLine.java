package org.eclipse.fx.ui.controls.styledtext_ng;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.eclipse.fx.core.Range;
import org.eclipse.fx.core.Subscription;
import org.eclipse.fx.ui.controls.styledtext.StyledString;
import org.eclipse.fx.ui.controls.styledtext.StyledStringSegment;

import javafx.css.CssMetaData;
import javafx.css.SimpleStyleableBooleanProperty;
import javafx.css.SimpleStyleableObjectProperty;
import javafx.css.Styleable;
import javafx.css.StyleableProperty;
import javafx.css.StyleablePropertyFactory;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;

public class StyledLine extends Region {
	private final StyledLineRenderer renderer;
	private final List<Subscription> ranges = new ArrayList<>();

	public StyledLine(StyledLineRendererFactory factory) {
		this.renderer = factory.createRenderer();
		Node node = renderer.getNode();
		getChildren().add(node);
	}

	@Override
	protected void layoutChildren() {
		getManagedChildren().forEach( c -> c.resizeRelocate(0, 0, c.prefWidth(-1), c.prefHeight(-1)));
	}

	public void setStyledString(StyledString string) {

		Map<List<String>, SegmentNode> map = new HashMap<>();
		renderer.combinedAction( () -> {
			int idx = 0;
			ranges.forEach(Subscription::dispose);
			for( StyledStringSegment s : string.getSegmentList() ) {
				SegmentNode node = map.computeIfAbsent(s.getStyleClass(), l -> new SegmentNode(l,this.renderer));
				this.ranges.add(node.addRange(new Range(idx, idx += s.getText().length())));
			}
			this.renderer.setText(string.toString().toCharArray());
		});

		getChildren().addAll( map.values() );
	}

	public void setFont(String family, double size) {
		this.renderer.setFont(family, size);
	}

	static class SegmentNode extends Group {
		final StyledLineRenderer renderer;

		final List<Range> ranges = new ArrayList<>();
		List<Subscription> boldSubscriptions = new ArrayList<>();
		List<Subscription> italicSubscriptions = new ArrayList<>();
		List<Subscription> fillSubscriptions = new ArrayList<>();

		public SegmentNode(List<String> styleclasses, StyledLineRenderer renderer) {
			setManaged(false);
			this.renderer = renderer;
			getStyleClass().setAll(styleclasses);
		}

		public Subscription addRange(Range r) {
			this.ranges.add(r);
			if( bold.getValue() ) {
				boldSubscriptions.add( this.renderer.setBold(r) );
			}

			if( italic.getValue() ) {
				italicSubscriptions.add(this.renderer.setItalic(r));
			}

			fillSubscriptions.add(this.renderer.setForeground(fill.getValue(), r));

			return () -> {
				if( bold.getValue() ) {
					boldSubscriptions.add( this.renderer.setBold(r) );
				}

				if( italic.getValue() ) {
					italicSubscriptions.add(this.renderer.setItalic(r));
				}

				fillSubscriptions.add(this.renderer.setForeground(fill.getValue(), r));

				this.ranges.remove(r);
			};
		}

		private static StyleablePropertyFactory<SegmentNode> FACTORY = new StyleablePropertyFactory<>(Group.getClassCssMetaData());

		private static final CssMetaData<SegmentNode, Boolean> BOLD = FACTORY.createBooleanCssMetaData("-efx-styled-bold", s -> s.bold, false, false);
		private static final CssMetaData<SegmentNode, Boolean> ITALIC = FACTORY.createBooleanCssMetaData("-efx-styled-italic", s -> s.italic, false, false);
		private static final CssMetaData<SegmentNode, Paint> FILL = FACTORY.createPaintCssMetaData("-efx-fill", s -> s.fill, Color.BLACK, false);

		private final StyleableProperty<Boolean> bold = new SimpleStyleableBooleanProperty(BOLD, this, "bold") {
			protected void invalidated() {
				super.invalidated();

				SegmentNode.this.renderer.combinedAction( () -> {
					SegmentNode.this.boldSubscriptions.forEach(Subscription::dispose);
					SegmentNode.this.boldSubscriptions.clear();
					if( get() ) {
						SegmentNode.this.boldSubscriptions = SegmentNode.this.ranges.stream().map( SegmentNode.this.renderer::setBold).collect(Collectors.toList());
					}
				});
			}
		};

		private final StyleableProperty<Boolean> italic = new SimpleStyleableBooleanProperty(ITALIC, this, "italic") {
			protected void invalidated() {
				super.invalidated();
				SegmentNode.this.renderer.combinedAction( () -> {
					SegmentNode.this.italicSubscriptions.forEach(Subscription::dispose);
					SegmentNode.this.italicSubscriptions.clear();
					if( get() ) {
						SegmentNode.this.italicSubscriptions = SegmentNode.this.ranges.stream().map( SegmentNode.this.renderer::setItalic).collect(Collectors.toList());
					}
				});
			}
		};

		private final StyleableProperty<Paint> fill = new SimpleStyleableObjectProperty<Paint>(FILL, this, "fill", Color.BLACK) {
			protected void invalidated() {
				super.invalidated();
				SegmentNode.this.renderer.combinedAction( () -> {
					Paint p = get();
					SegmentNode.this.ranges.forEach(r -> SegmentNode.this.renderer.setForeground(p, r));
				});
			}
		};

		public static List<CssMetaData<? extends Styleable, ?>> getClassCssMetaData() {
			return FACTORY.getCssMetaData();
		}

		@Override
		public List<CssMetaData<? extends Styleable, ?>> getCssMetaData() {
			return FACTORY.getCssMetaData();
		}

	}
}